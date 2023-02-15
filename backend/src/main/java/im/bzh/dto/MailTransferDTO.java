package im.bzh.dto;

import lombok.Data;

@Data
public class MailTransferDTO {

    private String type;
    private Integer[] ids;
    private String destination;

    private Boolean deletePermanently;

}
