#!/usr/bin/env bash
set -euo pipefail

: "${IMAGE_TAG:?Falta IMAGE_TAG}"
: "${EXPECTED_HOSTNAME:?Falta EXPECTED_HOSTNAME}"
CONTAINER_NAME=${CONTAINER_NAME:-apigp}
SPRING_PROFILE=${SPRING_PROFILE:-dev}
CONFIG_DIR=${CONFIG_DIR:-/opt/apigp/config}
HEALTHCHECK_URL=${HEALTHCHECK_URL:-}
export DOCKER_HOST=unix:///var/run/docker.sock

[[ $(hostname -s) == "$EXPECTED_HOSTNAME" ]] || {
  echo 'ERROR: el runner no está en el servidor esperado.' >&2
  exit 1
}
[[ $CONTAINER_NAME == apigp && $SPRING_PROFILE == dev && $CONFIG_DIR == /opt/apigp/config ]] || {
  echo 'ERROR: este script solo despliega apigp/dev en /opt/apigp/config.' >&2
  exit 1
}
command -v flock > /dev/null
if [[ -n $HEALTHCHECK_URL ]]; then command -v curl > /dev/null; fi
docker info > /dev/null

# No permite que otro proceso que use este script reemplace apigp a la vez.
lock_dir=${HOME:?}/.local/state/apigp
mkdir -p "$lock_dir"
exec 9> "$lock_dir/deploy.lock"
flock --nonblock 9 || { echo 'ERROR: ya hay un despliegue de apigp en curso.' >&2; exit 1; }

# Comprueba el archivo con el usuario real de la imagen, sin leer su contenido.
docker run --rm --network none \
  --mount "type=bind,src=$CONFIG_DIR,dst=/config,readonly" \
  --entrypoint /bin/sh "$IMAGE_TAG" \
  -c 'test -s "/config/application-$1.properties" && test -r "/config/application-$1.properties"' \
  sh "$SPRING_PROFILE"

backup="${CONTAINER_NAME}-previous-$(date +%s)-$$"
old_exists=false
old_running=false
old_stopped=false
old_renamed=false
replacement_attempted=false
committed=false

finish() {
  result=$?
  trap - EXIT INT TERM
  if [[ $committed == false ]]; then
    if [[ $replacement_attempted == true ]]; then
      if docker container inspect "$CONTAINER_NAME" > /dev/null 2>&1; then
        docker rm --force "$CONTAINER_NAME" > /dev/null || {
          echo "ERROR: no se pudo retirar el nuevo contenedor; anterior: $backup" >&2
          exit 1
        }
      fi
    fi
    if [[ $old_renamed == true ]]; then
      docker rename "$backup" "$CONTAINER_NAME" || {
        echo "ERROR: se requiere restaurar manualmente el contenedor $backup." >&2
        exit 1
      }
    fi
    if [[ $old_stopped == true && $old_running == true ]]; then
      if docker start "$CONTAINER_NAME" > /dev/null && \
        [[ $(docker inspect --format '{{.State.Running}}' "$CONTAINER_NAME") == true ]]; then
        echo 'Se restauró el contenedor anterior; el despliegue sigue marcado como fallido.' >&2
      else
        echo 'ERROR: no se pudo iniciar el contenedor anterior; requiere revisión manual.' >&2
      fi
    fi
    # Incluso si la restauración funciona, el nuevo despliegue no tuvo éxito.
    if [[ $result == 0 ]]; then result=1; fi
  fi
  exit "$result"
}
trap finish EXIT
trap 'exit 130' INT
trap 'exit 143' TERM

if docker container inspect "$CONTAINER_NAME" > /dev/null 2>&1; then
  old_exists=true
  old_running=$(docker inspect --format '{{.State.Running}}' "$CONTAINER_NAME")
  old_stopped=true
  docker stop --time 30 "$CONTAINER_NAME" > /dev/null
  docker rename "$CONTAINER_NAME" "$backup"
  old_renamed=true
fi

replacement_attempted=true
docker run --detach \
  --network host \
  --name "$CONTAINER_NAME" \
  --restart unless-stopped \
  --mount "type=bind,src=$CONFIG_DIR,dst=/config,readonly" \
  --env "SPRING_PROFILES_ACTIVE=$SPRING_PROFILE" \
  --env SPRING_CONFIG_ADDITIONAL_LOCATION=file:/config/ \
  "$IMAGE_TAG" > /dev/null

started=$SECONDS
ready=false
check_http() {
  local status
  status=$(curl --silent --output /dev/null --write-out '%{http_code}' --max-time 3 "$HEALTHCHECK_URL") || return 1
  [[ $status =~ ^2[0-9][0-9]$ ]]
}
while (( SECONDS - started < 120 )); do
  [[ $(docker inspect --format '{{.State.Running}}' "$CONTAINER_NAME") == true ]]
  [[ $(docker inspect --format '{{.RestartCount}}' "$CONTAINER_NAME") == 0 ]]
  if (( SECONDS - started >= 10 )); then
    if [[ -z $HEALTHCHECK_URL ]] || check_http; then
      ready=true
      break
    fi
  fi
  sleep 2
done
[[ $ready == true ]] || { echo 'ERROR: la nueva versión no pasó la verificación.' >&2; exit 1; }

committed=true
if [[ $old_exists == true ]]; then
  # El contenedor anterior se elimina solo después de verificar la nueva versión.
  docker rm "$backup" > /dev/null || echo "AVISO: limpiar manualmente $backup."
fi
echo "Despliegue completado: $CONTAINER_NAME ($SPRING_PROFILE), imagen $IMAGE_TAG."
if [[ -z $HEALTHCHECK_URL ]]; then
  echo 'AVISO: solo se verificó proceso sin reinicios durante 10 segundos; falta comprobación HTTP/SQL Server.'
fi
