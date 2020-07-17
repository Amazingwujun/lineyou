package com.jun.lineyou.entity.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author Jun
 * @date 2020-07-16 19:36
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class FriendVO {

    private String mobile;
    private String nickname;
    private Boolean online;
    private String avatar;
}
