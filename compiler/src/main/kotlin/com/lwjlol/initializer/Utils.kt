package com.lwjlol.initializer

import java.io.OutputStream

/**
 * @author luwenjie on 2022/10/11 17:47:03
 */
fun OutputStream?.emit(
    s: String,
    indent: String = "",
) {
    if (!LOG) return
    this?.appendText("$indent$s\n")
}
