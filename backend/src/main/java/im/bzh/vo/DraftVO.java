package im.bzh.vo;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class DraftVO {

    private String subject;
    private String[] address;
    private String content;

}
