package com.psk.mybooks.core.data

import com.psk.mybooks.core.model.Book

// TODO: 검색 API·Room 으로 만든 Repository 가 생기면 제거하고, 검색·즐겨찾기 ViewModel 이 UseCase 에서 가져온다
// 검색과 즐겨찾기 feature 가 함께 쓰므로 core:data 에 둔다.
// 표지는 임의 이미지(picsum.photos). 마지막 두 권은 불러오기 실패·URL 없음 표시를 확인하는 용도다.
fun dummyBooks(): List<Book> = listOf(
    Book(
        id = "9791100000000",
        title = "해리 포터와 마법사의 돌",
        authors = listOf("J.K. 롤링"),
        publisher = "문학수첩",
        price = 15000,
        status = "정상판매",
        thumbnailUrl = "https://picsum.photos/seed/9791100000000/240/360"
    ),
    Book(
        id = "9791100000001",
        title = "클린 코드",
        authors = listOf("로버트 C. 마틴"),
        publisher = "인사이트",
        price = 33000,
        status = "정상판매",
        thumbnailUrl = "https://picsum.photos/seed/9791100000001/240/360"
    ),
    Book(
        id = "9791100000002",
        title = "이펙티브 코틀린",
        authors = listOf("마르친 모스칼라"),
        publisher = "인사이트",
        price = 28000,
        status = "품절",
        thumbnailUrl = "https://picsum.photos/seed/9791100000002/240/360"
    ),
    Book(
        id = "9791100000003",
        title = "아주 긴 제목의 책이 들어가면 두 줄까지만 보이고 나머지는 말줄임 처리가 됩니다 정말로요",
        authors = listOf("홍길동", "김철수", "이영희"),
        publisher = "아주 긴 출판사 이름 주식회사",
        price = 1234000,
        status = "정상판매",
        thumbnailUrl = "https://picsum.photos/seed/9791100000003/240/360"
    ),
    Book(
        id = "9791100000004",
        title = "데미안",
        authors = listOf("헤르만 헤세"),
        publisher = "민음사",
        price = 9000,
        status = "정상판매",
        thumbnailUrl = "https://picsum.photos/seed/9791100000004/240/360"
    ),
    Book(
        id = "9791100000005",
        title = "어린 왕자",
        authors = listOf("앙투안 드 생텍쥐페리"),
        publisher = "열린책들",
        price = 8500,
        status = "정상판매",
        thumbnailUrl = "https://picsum.photos/seed/9791100000005/240/360"
    ),
    Book(
        id = "9791100000006",
        title = "사피엔스",
        authors = listOf("유발 하라리"),
        publisher = "김영사",
        price = 24000,
        status = "품절",
        thumbnailUrl = "https://picsum.photos/seed/9791100000006/240/360"
    ),
    Book(
        id = "9791100000007",
        title = "코스모스",
        authors = listOf("칼 세이건"),
        publisher = "사이언스북스",
        price = 22000,
        status = "정상판매",
        thumbnailUrl = "https://picsum.photos/seed/9791100000007/240/360"
    ),
    Book(
        id = "9791100000008",
        title = "총, 균, 쇠",
        authors = listOf("재레드 다이아몬드"),
        publisher = "문학사상",
        price = 25000,
        status = "정상판매",
        thumbnailUrl = "https://picsum.photos/seed/9791100000008/240/360"
    ),
    Book(
        id = "9791100000009",
        title = "호모 데우스",
        authors = listOf("유발 하라리"),
        publisher = "김영사",
        price = 26000,
        status = "정상판매",
        thumbnailUrl = "https://picsum.photos/seed/9791100000009/240/360"
    ),
    Book(
        id = "9791100000010",
        title = "1984",
        authors = listOf("조지 오웰"),
        publisher = "민음사",
        price = 11000,
        status = "정상판매",
        thumbnailUrl = "https://picsum.photos/seed/9791100000010/240/360"
    ),
    Book(
        id = "9791100000011",
        title = "동물농장",
        authors = listOf("조지 오웰"),
        publisher = "민음사",
        price = 7500,
        status = "정상판매",
        thumbnailUrl = "https://picsum.photos/seed/9791100000011/240/360"
    ),
    Book(
        id = "9791100000012",
        title = "채식주의자",
        authors = listOf("한강"),
        publisher = "창비",
        price = 13000,
        status = "정상판매",
        thumbnailUrl = "https://picsum.photos/seed/9791100000012/240/360"
    ),
    Book(
        id = "9791100000013",
        title = "소년이 온다",
        authors = listOf("한강"),
        publisher = "창비",
        price = 14000,
        status = "품절",
        thumbnailUrl = "https://picsum.photos/seed/9791100000013/240/360"
    ),
    Book(
        id = "9791100000014",
        title = "불편한 편의점",
        authors = listOf("김호연"),
        publisher = "나무옆의자",
        price = 14000,
        status = "정상판매",
        thumbnailUrl = "https://picsum.photos/seed/9791100000014/240/360"
    ),
    Book(
        id = "9791100000015",
        title = "달러구트 꿈 백화점",
        authors = listOf("이미예"),
        publisher = "팩토리나인",
        price = 13800,
        status = "정상판매",
        thumbnailUrl = "https://picsum.photos/seed/9791100000015/240/360"
    ),
    Book(
        id = "9791100000016",
        title = "아몬드",
        authors = listOf("손원평"),
        publisher = "창비",
        price = 12000,
        status = "정상판매",
        thumbnailUrl = "https://picsum.photos/invalid-url-for-load-failed"
    ),
    Book(
        id = "9791100000017",
        title = "트렌드 코리아 2026",
        authors = listOf("김난도"),
        publisher = "미래의창",
        price = 19000,
        status = "정상판매",
        thumbnailUrl = ""
    )
)
