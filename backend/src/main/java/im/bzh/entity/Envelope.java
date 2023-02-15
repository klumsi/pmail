package im.bzh.entity;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Envelope {
    private Long id;
    private String fromName;
    private String fromAddress;
    private String subject;
    private Integer status;
    private String date;
    private Long timestamp;
}
