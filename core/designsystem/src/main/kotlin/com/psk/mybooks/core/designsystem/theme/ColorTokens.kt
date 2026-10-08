package com.psk.mybooks.core.designsystem.theme

import androidx.compose.ui.graphics.Color

/*
 * Color Tokens (팔레트). 이름 = 색상 계열 + 톤(HCT tone, 0 어두움 ~ 100 밝음).
 * designsystem 내부 전용(internal). feature/컴포넌트는 MbColors(시맨틱)만 사용한다.
 *
 * 브랜드 컬러 #F83949 를 Material Color Utilities(Fidelity) 에 넣어 생성했다.
 * Gray 는 붉은 기가 섞이지 않도록 Monochrome 스킴의 무채색을 사용한다.
 */

// Red: 브랜드(primary) 계열
internal val Red10 = Color(0xFF440008)
internal val Red40 = Color(0xFFBF0028)
internal val Red49 = Color(0xFFE0263B)
internal val Red55 = Color(0xFFF83949) // 브랜드 컬러
internal val Red60 = Color(0xFFFF535A)
internal val Red80 = Color(0xFFFFB3B1)
internal val Red99 = Color(0xFFFFFBFF)

// Rose: secondary 계열 (Red 보다 채도가 낮음)
internal val Rose20 = Color(0xFF660513)
internal val Rose24 = Color(0xFF73111B)
internal val Rose31 = Color(0xFF892228)
internal val Rose40 = Color(0xFFA6383C)
internal val Rose67 = Color(0xFFFF7A7B)
internal val Rose75 = Color(0xFFFF9E9C)

// Teal: tertiary 계열
internal val Teal11 = Color(0xFF00231E)
internal val Teal20 = Color(0xFF003731)
internal val Teal39 = Color(0xFF00685C)
internal val Teal49 = Color(0xFF008375)
internal val Teal60 = Color(0xFF21A291)
internal val Teal80 = Color(0xFF67D9C7)
internal val Teal99 = Color(0xFFF4FFFB)

// Error
internal val Error20 = Color(0xFF690005)
internal val Error30 = Color(0xFF93000A)
internal val Error40 = Color(0xFFBA1A1A)
internal val Error80 = Color(0xFFFFB4AB)
internal val Error90 = Color(0xFFFFDAD6)

// Gray: 무채색
internal val Gray0 = Color(0xFF000000)
internal val Gray4 = Color(0xFF0E0E0E)
internal val Gray6 = Color(0xFF131313)
internal val Gray10 = Color(0xFF1B1B1B)
internal val Gray12 = Color(0xFF1F1F1F)
internal val Gray17 = Color(0xFF2A2A2A)
internal val Gray20 = Color(0xFF303030)
internal val Gray22 = Color(0xFF353535)
internal val Gray24 = Color(0xFF393939)
internal val Gray30 = Color(0xFF474747)
internal val Gray50 = Color(0xFF777777)
internal val Gray60 = Color(0xFF919191)
internal val Gray80 = Color(0xFFC6C6C6)
internal val Gray87 = Color(0xFFDADADA)
internal val Gray90 = Color(0xFFE2E2E2)
internal val Gray92 = Color(0xFFE8E8E8)
internal val Gray94 = Color(0xFFEEEEEE)
internal val Gray95 = Color(0xFFF1F1F1)
internal val Gray96 = Color(0xFFF3F3F3)
internal val Gray98 = Color(0xFFF9F9F9)
internal val Gray100 = Color(0xFFFFFFFF)
