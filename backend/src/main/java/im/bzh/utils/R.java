package im.bzh.utils;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class R {
    private Boolean success;
    private String msg;
    private Object data;
}
