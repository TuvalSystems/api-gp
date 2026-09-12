package tuval.api.web.cobranzas.cobros.entity.tuval;

import java.util.List;

import tuval.api.web.cobranzas.cobros.dto.ClientOverdueDocumentDto;

public class ListClientOverdueDocumentEntity {

  public List<ClientOverdueDocumentDto> data;

  public List<ClientOverdueDocumentDto> getData() {
    return data;
  }

  public void setData(List<ClientOverdueDocumentDto> data) {
    this.data = data;
  }
}
