package com.psk.mybooks.dev

import android.content.Context
import androidx.compose.runtime.Composer
import dalvik.system.DexFile

/** 찾은 @Preview 함수 하나. [facadeClass] 의 [method] 를 PreviewActivity 에 그대로 넘긴다. */
internal data class PreviewEntry(
    val facadeClass: String,
    val method: String
) {
    /** PreviewActivity 가 받는 `클래스.함수` 이름 */
    val composableName: String get() = "$facadeClass.$method"

    /** 화면에 보일 묶음 이름. 패키지에서 앱 공통 접두어를 뺀다. */
    val group: String get() = facadeClass
        .removePrefix("com.psk.mybooks.")
        .substringBeforeLast('.')
}

private const val APP_PACKAGE_PREFIX: String = "com.psk.mybooks."

/**
 * 앱 코드에서 @Preview 함수를 찾는다. @Preview 는 실행 중에 읽을 수 없는 어노테이션이라, 파일 클래스(`...Kt`)의
 * Composable 함수 중 이름에 `Preview` 가 들어가고 다른 파라미터가 없는 것을 Preview 로 본다. 파라미터를 받는 도우미 함수는 열 수 없어서 뺀다.
 * 목록을 손으로 적어 두지 않으므로 Preview 가 늘거나 이름이 바뀌어도 어긋나지 않는다.
 */
@Suppress("DEPRECATION")
internal fun scanPreviews(context: Context): List<PreviewEntry> {
    val loader: ClassLoader = context.classLoader
    val apkPaths: List<String> = listOf(context.applicationInfo.sourceDir) +
        (
            context.applicationInfo.splitSourceDirs
                ?.toList() ?: emptyList()
            )
    val result: MutableList<PreviewEntry> = mutableListOf()
    for (path in apkPaths) {
        val dex: DexFile = DexFile(path)
        try {
            for (name in dex.entries()) {
                if (!name.startsWith(APP_PACKAGE_PREFIX) || !name.endsWith("Kt")) continue
                val type: Class<*> = try {
                    Class.forName(name, false, loader)
                } catch (e: ReflectiveOperationException) {
                    continue
                } catch (e: LinkageError) {
                    continue
                }
                for (method in type.declaredMethods) {
                    val isComposable: Boolean = method.parameterTypes.let { types ->
                        types.size == 2 && types[0] == Composer::class.java
                    }
                    if (isComposable && "Preview" in method.name && '$' !in method.name) {
                        result += PreviewEntry(facadeClass = name, method = method.name)
                    }
                }
            }
        } finally {
            dex.close()
        }
    }
    return result.sortedWith(compareBy({ it.group }, { it.method }))
}
