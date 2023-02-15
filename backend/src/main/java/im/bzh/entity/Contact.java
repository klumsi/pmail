package im.bzh.entity;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Contact {
    private Long id;
    private String nickname;
    private String address;
    private String creator;
}
