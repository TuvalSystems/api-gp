package tuval.api.web.models.entity.tuval.cobros;

import java.util.List;

import tuval.api.web.models.dto.cobros.ClientOverdueDocumentDto;

public class ListClientOverdueDocumentEntity {

  public List<ClientOverdueDocumentDto> data;

  public List<ClientOverdueDocumentDto> getData() {
    return data;
  }

  public void setData(List<ClientOverdueDocumentDto> data) {
    this.data = data;
  }
}
