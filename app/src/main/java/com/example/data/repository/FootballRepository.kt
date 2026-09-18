package com.example.data.repository

import com.example.data.local.CommentDao
import com.example.data.local.CommentEntity
import com.example.data.model.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class FootballRepository(private val commentDao: CommentDao) {

    fun getCommentsForTarget(targetId: String): Flow<List<UserComment>> {
        return commentDao.getCommentsByTarget(targetId).map { entities ->
            entities.map { it.toUserComment() }
        }
    }

    suspend fun addComment(targetId: String, authorName: String, userTeam: String, content: String): Long {
        val entity = CommentEntity(
            targetId = targetId,
            authorName = authorName,
            userTeam = userTeam,
            content = content,
            timestamp = System.currentTimeMillis(),
            likesCount = 0,
            isLiked = false
        )
        return commentDao.insertComment(entity)
    }

    suspend fun toggleLike(commentId: Long, currentLiked: Boolean) {
        val newLiked = !currentLiked
        val delta = if (newLiked) 1 else -1
        commentDao.updateLike(commentId, delta, newLiked)
    }

    suspend fun seedInitialCommentsIfEmpty() {
        // We'll insert a few engaging starter comments if not present
        val initialComments = listOf(
            CommentEntity(
                targetId = "match_1",
                authorName = "Burak Yılmazer",
                userTeam = "Galatasaray",
                content = "Osimhen ve Mertens uyumu inanılmaz seviyede. İkinci yarıdaki pres rakibe nefes aldırmadı.",
                timestamp = System.currentTimeMillis() - 3600000 * 4,
                likesCount = 42,
                isLiked = false
            ),
            CommentEntity(
                targetId = "match_1",
                authorName = "Caner Tekin",
                userTeam = "Fenerbahçe",
                content = "İlk yarıda yakaladığımız net fırsatları harcadık. Taktiksel geçişlerde orta saha boş kaldı, acil revizyon gerek.",
                timestamp = System.currentTimeMillis() - 3600000 * 2,
                likesCount = 18,
                isLiked = false
            ),
            CommentEntity(
                targetId = "match_2",
                authorName = "Emre Kartal",
                userTeam = "Beşiktaş",
                content = "Semih Kılıçsoy'un bitiriciliği her maç daha da olgunlaşıyor. Hak edilmiş 3 puan!",
                timestamp = System.currentTimeMillis() - 3600000 * 8,
                likesCount = 35,
                isLiked = false
            ),
            CommentEntity(
                targetId = "news_1",
                authorName = "Murat Demir",
                userTeam = "Trabzonspor",
                content = "Lig bu sene son haftaya kadar kopmayacak gibi duruyor. Anadolu takımları da çok dirençli.",
                timestamp = System.currentTimeMillis() - 3600000 * 5,
                likesCount = 12,
                isLiked = false
            )
        )
        for (c in initialComments) {
            commentDao.insertComment(c)
        }
    }

    fun getMatches(): List<MatchSummary> = listOf(
        MatchSummary(
            id = "match_1",
            league = "Trendyol Süper Lig",
            round = "26. Hafta",
            matchDate = "Dün",
            statusText = "MS",
            isLive = false,
            homeTeam = "Galatasaray",
            awayTeam = "Fenerbahçe",
            homeScore = 3,
            awayScore = 1,
            homeColorHex = 0xFFA90432, // Crimson Red
            awayColorHex = 0xFF002D62, // Navy Blue
            venue = "RAMS Park, İstanbul",
            referee = "Halil Umut Meler",
            summaryText = "Nefes kesen derbide Galatasaray, Osimhen ve Mertens'in muazzam oyunuyla 3-1 galip gelerek zirvedeki yerini sağlamlaştırdı. Fenerbahçe'nin tek golü Dzeko'nun penaltısıyla geldi.",
            videoHighlightDuration = "09:45",
            mvpPlayer = "Victor Osimhen (8.9)",
            goals = listOf(
                GoalEvent(19, "Gabriel Sara", "Galatasaray"),
                GoalEvent(38, "Victor Osimhen", "Galatasaray"),
                GoalEvent(54, "Edin Dzeko (P)", "Fenerbahçe", isPenalty = true),
                GoalEvent(82, "Dries Mertens", "Galatasaray")
            ),
            cards = listOf(
                CardEvent(24, "Lucas Torreira", "Galatasaray"),
                CardEvent(45, "Alexander Djiku", "Fenerbahçe"),
                CardEvent(71, "Fred", "Fenerbahçe"),
                CardEvent(89, "Abdülkerim Bardakcı", "Galatasaray")
            ),
            timeline = listOf(
                TimelineEvent(19, TimelineEventType.GOAL, "GOL! Gabriel Sara", "Ceza yayından köşeye harika plase vuruşla ağları havalandırdı.", "Galatasaray"),
                TimelineEvent(32, TimelineEventType.CHANCE, "Direkten Döndü!", "Tadic'in sert şutunda top üst direkten oyun alanına geri döndü.", "Fenerbahçe"),
                TimelineEvent(38, TimelineEventType.GOAL, "GOL! Victor Osimhen", "Barış Alper'in ortasında muazzam kafa vuruşuyla skoru 2-0 yaptı.", "Galatasaray"),
                TimelineEvent(52, TimelineEventType.VAR_DECISION, "VAR İncelemesi: Penaltı", "Hakem ceza sahası içindeki müdahaleyi izleyerek penaltı noktasını gösterdi.", "Fenerbahçe"),
                TimelineEvent(54, TimelineEventType.GOAL, "GOL! Edin Dzeko (P)", "Dzeko soğukkanlı bir vuruşla kaleciyi ters köşeye yatırdı.", "Fenerbahçe"),
                TimelineEvent(82, TimelineEventType.GOAL, "GOL! Dries Mertens", "Hızlı kontratakta ceza sahasında aşırtma vuruşla maçın skorunu belirledi.", "Galatasaray")
            ),
            stats = MatchStatistics(
                possessionHome = 54,
                possessionAway = 46,
                shotsHome = 16,
                shotsAway = 12,
                shotsOnTargetHome = 7,
                shotsOnTargetAway = 4,
                xGHome = 2.34f,
                xGAway = 1.42f,
                cornersHome = 6,
                cornersAway = 4,
                foulsHome = 14,
                foulsAway = 16,
                passAccuracyHome = 84,
                passAccuracyAway = 81
            ),
            homeFormation = "4-2-3-1",
            awayFormation = "4-2-3-1",
            homeStarters = listOf(
                LineupPlayer(1, "Muslera", "KL", 7.4f),
                LineupPlayer(23, "Kaan Ayhan", "DF", 7.2f),
                LineupPlayer(6, "Davinson", "DF", 8.1f),
                LineupPlayer(42, "Abdülkerim", "DF", 7.5f),
                LineupPlayer(4, "Jakobs", "DF", 7.3f),
                LineupPlayer(34, "Torreira", "OS", 8.0f),
                LineupPlayer(20, "Sara", "OS", 8.5f),
                LineupPlayer(53, "Barış Alper", "OS", 7.9f),
                LineupPlayer(10, "Mertens", "OS", 8.6f),
                LineupPlayer(11, "Yunus Akgün", "OS", 7.7f),
                LineupPlayer(45, "Osimhen", "FV", 8.9f)
            ),
            awayStarters = listOf(
                LineupPlayer(40, "Livakovic", "KL", 6.8f),
                LineupPlayer(16, "Mert Müldür", "DF", 6.6f),
                LineupPlayer(6, "Djiku", "DF", 6.9f),
                LineupPlayer(50, "Becao", "DF", 6.7f),
                LineupPlayer(24, "Oosterwolde", "DF", 7.0f),
                LineupPlayer(5, "İsmail", "OS", 6.8f),
                LineupPlayer(13, "Fred", "OS", 7.4f),
                LineupPlayer(10, "Tadic", "OS", 7.5f),
                LineupPlayer(53, "Szymanski", "OS", 6.9f),
                LineupPlayer(97, "Saint-Maximin", "OS", 7.3f),
                LineupPlayer(9, "Dzeko", "FV", 7.6f)
            )
        ),
        MatchSummary(
            id = "match_2",
            league = "Trendyol Süper Lig",
            round = "26. Hafta",
            matchDate = "Dün",
            statusText = "MS",
            isLive = false,
            homeTeam = "Beşiktaş",
            awayTeam = "Trabzonspor",
            homeScore = 2,
            awayScore = 1,
            homeColorHex = 0xFF111111, // Black
            awayColorHex = 0xFF800020, // Burgundy
            venue = "Tüpraş Stadyumu, İstanbul",
            referee = "Ali Şansalan",
            summaryText = "Tüpraş Stadyumu'nda tempolu geçen mücadelede Beşiktaş, genç golcüsü Semih Kılıçsoy'un yıldızlaştığı gecede Trabzonspor'u 2-1 mağlup etti.",
            videoHighlightDuration = "07:20",
            mvpPlayer = "Semih Kılıçsoy (8.7)",
            goals = listOf(
                GoalEvent(28, "Semih Kılıçsoy", "Beşiktaş"),
                GoalEvent(63, "Simon Banza", "Trabzonspor"),
                GoalEvent(77, "Semih Kılıçsoy", "Beşiktaş")
            ),
            cards = listOf(
                CardEvent(41, "Gedson Fernandes", "Beşiktaş"),
                CardEvent(59, "Stefan Savic", "Trabzonspor"),
                CardEvent(84, "Batagov", "Trabzonspor", isRed = true)
            ),
            timeline = listOf(
                TimelineEvent(28, TimelineEventType.GOAL, "GOL! Semih Kılıçsoy", "Rafa Silva'nın ara pasında kaleci Uğurcan'ı çalımlayıp topu filelere gönderdi.", "Beşiktaş"),
                TimelineEvent(63, TimelineEventType.GOAL, "GOL! Simon Banza", "Visca'nın köşe vuruşunda kafayla beraberliği sağladı.", "Trabzonspor"),
                TimelineEvent(77, TimelineEventType.GOAL, "GOL! Semih Kılıçsoy", "Ceza sahası dışından mükemmel bir vole ile üst köşeyi buldu.", "Beşiktaş"),
                TimelineEvent(84, TimelineEventType.RED_CARD, "KIRMIZI KART! Batagov", "Son adam müdahalesi gerekçesiyle doğrudan kırmızı kartla oyun dışı kaldı.", "Trabzonspor")
            ),
            stats = MatchStatistics(
                possessionHome = 58,
                possessionAway = 42,
                shotsHome = 14,
                shotsAway = 9,
                shotsOnTargetHome = 6,
                shotsOnTargetAway = 3,
                xGHome = 2.15f,
                xGAway = 1.10f,
                cornersHome = 7,
                cornersAway = 3,
                foulsHome = 12,
                foulsAway = 15,
                passAccuracyHome = 86,
                passAccuracyAway = 78
            )
        ),
        MatchSummary(
            id = "match_3",
            league = "UEFA Şampiyonlar Ligi",
            round = "Çeyrek Final",
            matchDate = "Canlı",
            statusText = "CANLI 76'",
            isLive = true,
            homeTeam = "Real Madrid",
            awayTeam = "Manchester City",
            homeScore = 2,
            awayScore = 2,
            homeColorHex = 0xFFF3F4F6, // White
            awayColorHex = 0xFF6CABDD, // Sky Blue
            venue = "Santiago Bernabéu, Madrid",
            referee = "Szymon Marciniak",
            summaryText = "Bernabéu'da akıl almaz bir futbol şöleni yaşanıyor. Karşılıklı fırtına gibi geçen atakta tempo bir an bile düşmedi.",
            videoHighlightDuration = "11:15",
            mvpPlayer = "Vinicius Jr (8.8)",
            goals = listOf(
                GoalEvent(12, "Bernardo Silva", "Manchester City"),
                GoalEvent(22, "Vinicius Jr", "Real Madrid"),
                GoalEvent(59, "Rodrygo", "Real Madrid"),
                GoalEvent(66, "Phil Foden", "Manchester City")
            ),
            cards = listOf(
                CardEvent(35, "Carvajal", "Real Madrid"),
                CardEvent(62, "Rodri", "Manchester City")
            ),
            timeline = listOf(
                TimelineEvent(12, TimelineEventType.GOAL, "GOL! Bernardo Silva", "Frikikten zekice barajın altından ağları buldu.", "Manchester City"),
                TimelineEvent(22, TimelineEventType.GOAL, "GOL! Vinicius Jr", "Hızlı kontrada 3 oyuncuyu geçip plaseyi köşeye bıraktı.", "Real Madrid"),
                TimelineEvent(59, TimelineEventType.GOAL, "GOL! Rodrygo", "Bellingham'ın asistinde düzgün vuruş.", "Real Madrid"),
                TimelineEvent(66, TimelineEventType.GOAL, "GOL! Phil Foden", "Ceza yayı üzerinden doksana giden olağanüstü şut!", "Manchester City")
            ),
            stats = MatchStatistics(
                possessionHome = 44,
                possessionAway = 56,
                shotsHome = 15,
                shotsAway = 18,
                shotsOnTargetHome = 8,
                shotsOnTargetAway = 9,
                xGHome = 2.40f,
                xGAway = 2.65f,
                cornersHome = 5,
                cornersAway = 8,
                foulsHome = 9,
                foulsAway = 11,
                passAccuracyHome = 87,
                passAccuracyAway = 91
            )
        ),
        MatchSummary(
            id = "match_4",
            league = "Trendyol Süper Lig",
            round = "26. Hafta",
            matchDate = "Önceki Gün",
            statusText = "MS",
            isLive = false,
            homeTeam = "Samsunspor",
            awayTeam = "Başakşehir",
            homeScore = 2,
            awayScore = 0,
            homeColorHex = 0xFFDC2626, // Red
            awayColorHex = 0xFFF97316, // Orange
            venue = "Samsun 19 Mayıs Stadyumu",
            referee = "Zorbay Küçük",
            summaryText = "Ligin flaş ekibi Samsunspor, taraftarının müthiş desteğiyle Başakşehir'i net bir skorla mağlup etti.",
            videoHighlightDuration = "06:10",
            mvpPlayer = "Marius Mouandilmadji (8.2)",
            goals = listOf(
                GoalEvent(34, "Ntcham", "Samsunspor"),
                GoalEvent(88, "Marius", "Samsunspor")
            ),
            cards = listOf(
                CardEvent(45, "Opoku", "Başakşehir")
            ),
            timeline = listOf(
                TimelineEvent(34, TimelineEventType.GOAL, "GOL! Ntcham", "Penaltı vuruşunda topu köşeye yolladı.", "Samsunspor"),
                TimelineEvent(88, TimelineEventType.GOAL, "GOL! Marius", "Karşı karşıya pozisyonda soğukkanlı bitiriş.", "Samsunspor")
            ),
            stats = MatchStatistics(
                possessionHome = 49,
                possessionAway = 51,
                shotsHome = 11,
                shotsAway = 8,
                shotsOnTargetHome = 5,
                shotsOnTargetAway = 2,
                xGHome = 1.80f,
                xGAway = 0.75f,
                cornersHome = 4,
                cornersAway = 4,
                foulsHome = 13,
                foulsAway = 14,
                passAccuracyHome = 80,
                passAccuracyAway = 82
            )
        ),
        MatchSummary(
            id = "match_5",
            league = "Trendyol Süper Lig",
            round = "26. Hafta",
            matchDate = "Canlı",
            statusText = "CANLI 38'",
            isLive = true,
            homeTeam = "Trabzonspor",
            awayTeam = "Göztepe",
            homeScore = 1,
            awayScore = 0,
            homeColorHex = 0xFF800020, // Burgundy
            awayColorHex = 0xFFFBBF24, // Yellow
            venue = "Papara Park, Trabzon",
            referee = "Kadir Sağlam",
            summaryText = "Papara Park'ta Trabzonspor, Simon Banza'nın kafa golüyle 1-0 önde. Karadeniz fırtınası baskısını sürdürüyor.",
            videoHighlightDuration = "Canlı Yayın",
            mvpPlayer = "Simon Banza (7.9)",
            goals = listOf(
                GoalEvent(21, "Simon Banza", "Trabzonspor")
            ),
            cards = listOf(
                CardEvent(29, "Dennis", "Göztepe")
            ),
            timeline = listOf(
                TimelineEvent(21, TimelineEventType.GOAL, "GOL! Simon Banza", "Visca'nın nefis ortasında harika kafa vuruşuyla topu ağlara yolladı.", "Trabzonspor"),
                TimelineEvent(29, TimelineEventType.YELLOW_CARD, "SARI KART! Dennis", "Orta alandaki sert müdahale sonrası kart gördü.", "Göztepe"),
                TimelineEvent(36, TimelineEventType.SUBSTITUTION, "Tehlikeli Atak", "Uğurcan Çakır köşeden uzanarak gole izin vermedi.", "Göztepe")
            ),
            stats = MatchStatistics(
                possessionHome = 62,
                possessionAway = 38,
                shotsHome = 7,
                shotsAway = 3,
                shotsOnTargetHome = 4,
                shotsOnTargetAway = 1,
                xGHome = 1.25f,
                xGAway = 0.35f,
                cornersHome = 4,
                cornersAway = 1,
                foulsHome = 6,
                foulsAway = 9,
                passAccuracyHome = 84,
                passAccuracyAway = 73
            )
        )
    )

    fun getNews(): List<NewsArticle> = listOf(
        NewsArticle(
            id = "news_1",
            title = "Süper Lig'de Zirve Savaşı: Şampiyonluk Düğümü Hangi Taktikle Çözülecek?",
            summary = "Süper Lig'in 26. haftası geride kalırken lider ve takipçileri arasındaki puan farkı kapanıyor. Takımların hücum üretkenlikleri ve xG verileri mercek altında.",
            content = "Trendyol Süper Lig'de son dönemece girilirken şampiyonluk yarışındaki rekabet tarihi bir seviyeye ulaştı. Galatasaray'ın ön alan baskısındaki fiziksel üstünlüğü, Fenerbahçe'nin kanat organizasyonları ve Beşiktaş'ın Rafa Silva merkezli geçiş hücumları ligin kaderini tayin ediyor. Uzman analistlere göre kalan 12 haftada duran top organizasyonları ve derin rotasyon kritik rol oynayacak.",
            category = "Süper Lig",
            author = "Alp Bilgin (Taktik Başyazarı)",
            publishTime = "32 dk önce",
            readTimeMinutes = 4,
            bannerAccentColor = 0xFF059669,
            tags = listOf("Süper Lig", "Taktik", "Şampiyonluk", "Analiz"),
            isBreaking = true
        ),
        NewsArticle(
            id = "news_2",
            title = "Transfer Bombası: Premier Lig Devi Milli Yıldız İçin Masada!",
            summary = "Avrupa'da fırtınalar estiren milli futbolcumuz için 45 milyon Euro'luk teklif hazırlandı.",
            content = "İngiltere Premier Lig'in köklü kulüplerinden Arsenal ve Liverpool'un scout ekipleri, milli futbolcumuzun son 6 maçını tribünden canlı takip etti. Gelen bilgilere göre kulüp yönetimi sezon sonu için resmi temaslara başlamak üzere menajeriyle bir araya geldi. Transferin Türk futbol tarihinin en yüksek bonservis rekorunu kırması bekleniyor.",
            category = "Transfer",
            author = "Serdar Engin",
            publishTime = "2 saat önce",
            readTimeMinutes = 3,
            bannerAccentColor = 0xFF2563EB,
            tags = listOf("Transfer", "Premier Lig", "Milli Takım")
        ),
        NewsArticle(
            id = "news_3",
            title = "Yarı Otomatik Ofsayt ve VAR İncelemeleri: Bu Hafta Neler Değişti?",
            summary = "MHK ve Hakem Gözlemcileri haftanın kritik tartışmalı pozisyonlarını ve VAR kararlarını değerlendirdi.",
            content = "Hafta sonu oynanan derbi maçında verilen penaltı kararı ve çizilen ofsayt çizgileri teknolojinin sunduğu hassas açı verileriyle doğrulandı. MHK Başkanı yaptığı açıklamada, karar sürelerinin ortalama 42 saniyeye indiğini belirterek ligdeki adaletin teknolojik yatırımlarla güçlendiğini vurguladı.",
            category = "Süper Lig",
            author = "Deniz Aktaş",
            publishTime = "5 saat önce",
            readTimeMinutes = 5,
            bannerAccentColor = 0xFFD97706,
            tags = listOf("VAR", "Hakem", "MHK")
        ),
        NewsArticle(
            id = "news_4",
            title = "Şampiyonlar Ligi'nde Dev Randevu: Taktiksel Dizilimler ve Eşleşmeler",
            summary = "Avrupa'nın bir numaralı kupasında yarı finale giden yolda dev takımların analizi.",
            content = "Şampiyonlar Ligi çeyrek final eşleşmelerinde takımların pres yoğunluğu, savunma arkasına koşular ve kilit pas istatistikleri incelendi. Real Madrid - Manchester City eşleşmesi modern futbolun taktiksel zirvesi olarak nitelendiriliyor.",
            category = "Avrupa",
            author = "Mehmet Özkan",
            publishTime = "7 saat önce",
            readTimeMinutes = 4,
            bannerAccentColor = 0xFF7C3AED,
            tags = listOf("Şampiyonlar Ligi", "UEFA", "Real Madrid", "Man City")
        ),
        NewsArticle(
            id = "news_5",
            title = "A Milli Takım'da Yeni Dönem: Genç Yetenekler Kadroya Dahil Ediliyor",
            summary = "Dünya Kupası Elemeleri grup aşaması öncesi teknik heyet genç oyuncu havuzunu genişletiyor.",
            content = "Milli Takım Teknik Direktörü, hem yurt içinde parlayan hem de Avrupa akademilerinde forma giyen Türk gençlerini aday kadroya davet etti. Orta saha dinamizmini artırmayı hedefleyen yeni oyun planı basına tanıtıldı.",
            category = "Milli Takım",
            author = "Kaan Yıldız",
            publishTime = "1 gün önce",
            readTimeMinutes = 3,
            bannerAccentColor = 0xFFDC2626,
            tags = listOf("Milli Takım", "Dünya Kupası", "Türkiye")
        )
    )

    fun getStandings(): List<LeagueStanding> = listOf(
        LeagueStanding(1, "Galatasaray", "GS", 0xFFA90432, 26, 21, 4, 1, 62, 18, 44, 67, listOf('G', 'G', 'G', 'B', 'G'), QualificationType.CHAMPIONS_LEAGUE),
        LeagueStanding(2, "Fenerbahçe", "FB", 0xFF002D62, 26, 19, 4, 3, 58, 22, 36, 61, listOf('G', 'G', 'B', 'G', 'M'), QualificationType.CHAMPIONS_LEAGUE),
        LeagueStanding(3, "Samsunspor", "SAM", 0xFFDC2626, 26, 15, 5, 6, 42, 26, 16, 50, listOf('G', 'G', 'B', 'G', 'G'), QualificationType.EUROPA_LEAGUE),
        LeagueStanding(4, "Beşiktaş", "BJK", 0xFF111111, 26, 14, 6, 6, 45, 27, 18, 48, listOf('B', 'M', 'G', 'G', 'G'), QualificationType.CONFERENCE_LEAGUE),
        LeagueStanding(5, "Eyüpspor", "EYP", 0xFF854D0E, 26, 12, 7, 7, 39, 31, 8, 43, listOf('G', 'B', 'M', 'G', 'B'), QualificationType.NORMAL),
        LeagueStanding(6, "Trabzonspor", "TS", 0xFF800020, 26, 11, 8, 7, 38, 30, 8, 41, listOf('G', 'B', 'G', 'B', 'M'), QualificationType.NORMAL),
        LeagueStanding(7, "Başakşehir", "IBFK", 0xFFF97316, 26, 11, 6, 9, 37, 34, 3, 39, listOf('M', 'G', 'G', 'M', 'M'), QualificationType.NORMAL),
        LeagueStanding(8, "Göztepe", "GÖZ", 0xFFEAB308, 26, 10, 7, 9, 36, 33, 3, 37, listOf('B', 'G', 'M', 'B', 'G'), QualificationType.NORMAL),
        LeagueStanding(9, "Sivasspor", "SİV", 0xFFB91C1C, 26, 9, 6, 11, 32, 38, -6, 33, listOf('M', 'M', 'G', 'B', 'G'), QualificationType.NORMAL),
        LeagueStanding(10, "Kasımpaşa", "KAS", 0xFF1E3A8A, 26, 8, 8, 10, 35, 41, -6, 32, listOf('B', 'B', 'M', 'G', 'M'), QualificationType.NORMAL),
        LeagueStanding(11, "Antalyaspor", "ANT", 0xFFDC2626, 26, 9, 4, 13, 30, 42, -12, 31, listOf('G', 'M', 'M', 'M', 'G'), QualificationType.NORMAL),
        LeagueStanding(12, "Rizespor", "RİZ", 0xFF15803D, 26, 8, 6, 12, 29, 39, -10, 30, listOf('M', 'G', 'B', 'M', 'B'), QualificationType.NORMAL),
        LeagueStanding(13, "Konyaspor", "KON", 0xFF166534, 26, 7, 7, 12, 28, 40, -12, 28, listOf('B', 'M', 'G', 'M', 'B'), QualificationType.NORMAL),
        LeagueStanding(14, "Alanyaspor", "ALN", 0xFFEA580C, 26, 6, 9, 11, 26, 38, -12, 27, listOf('B', 'B', 'M', 'B', 'M'), QualificationType.NORMAL),
        LeagueStanding(15, "Gaziantep FK", "GFK", 0xFF991B1B, 26, 6, 8, 12, 27, 41, -14, 26, listOf('M', 'B', 'M', 'G', 'M'), QualificationType.NORMAL),
        LeagueStanding(16, "Bodrum FK", "BOD", 0xFF0284C7, 26, 6, 6, 14, 20, 36, -16, 24, listOf('M', 'M', 'B', 'M', 'M'), QualificationType.NORMAL),
        LeagueStanding(17, "Kayserispor", "KAY", 0xFFCA8A04, 26, 5, 8, 13, 25, 46, -21, 23, listOf('M', 'B', 'M', 'B', 'M'), QualificationType.RELEGATION),
        LeagueStanding(18, "Hatayspor", "HAT", 0xFF7F1D1D, 26, 4, 8, 14, 23, 44, -21, 20, listOf('M', 'M', 'B', 'M', 'M'), QualificationType.RELEGATION),
        LeagueStanding(19, "Adana Demirspor", "ADS", 0xFF0284C7, 26, 2, 4, 20, 18, 55, -37, 7, listOf('M', 'M', 'M', 'M', 'M'), QualificationType.RELEGATION)
    )

    fun getTopScorers(): List<PlayerStatLeader> = listOf(
        PlayerStatLeader(1, "Victor Osimhen", "Galatasaray", 18, "19 Maç (3 Pen)", 0xFFA90432),
        PlayerStatLeader(2, "Edin Dzeko", "Fenerbahçe", 16, "22 Maç (4 Pen)", 0xFF002D62),
        PlayerStatLeader(3, "Ciro Immobile", "Beşiktaş", 14, "20 Maç (5 Pen)", 0xFF111111),
        PlayerStatLeader(4, "Krzysztof Piatek", "Başakşehir", 13, "24 Maç (2 Pen)", 0xFFF97316),
        PlayerStatLeader(5, "Simon Banza", "Trabzonspor", 12, "18 Maç (1 Pen)", 0xFF800020),
        PlayerStatLeader(6, "Marius Mouandilmadji", "Samsunspor", 11, "23 Maç", 0xFFDC2626)
    )

    fun getTopAssists(): List<PlayerStatLeader> = listOf(
        PlayerStatLeader(1, "Dries Mertens", "Galatasaray", 12, "23 Maç", 0xFFA90432),
        PlayerStatLeader(2, "Dusan Tadic", "Fenerbahçe", 11, "25 Maç", 0xFF002D62),
        PlayerStatLeader(3, "Rafa Silva", "Beşiktaş", 9, "22 Maç", 0xFF111111),
        PlayerStatLeader(4, "Gabriel Sara", "Galatasaray", 8, "24 Maç", 0xFFA90432),
        PlayerStatLeader(5, "Edin Visca", "Trabzonspor", 8, "21 Maç", 0xFF800020),
        PlayerStatLeader(6, "Allan Saint-Maximin", "Fenerbahçe", 7, "20 Maç", 0xFF002D62)
    )

    fun getTeamAnalyses(): List<TeamAnalysis> = listOf(
        TeamAnalysis(
            teamId = "gs",
            teamName = "Galatasaray",
            shortName = "GS",
            primaryColorHex = 0xFFA90432,
            secondaryColorHex = 0xFFFDB913,
            manager = "Okan Buruk",
            formation = "4-2-3-1 / 3-5-2",
            tacticalStyle = "Yüksek Ön Alan Baskısı & Agresif Geçiş",
            summary = "Ligin en yüksek xG (beklenen gol) ve rakip ceza sahasında topla buluşma oranına sahip takımı. Orta sahada Torreira-Sara ikilisiyle geçişleri domine ediyor.",
            attackRating = 92,
            defenseRating = 84,
            possessionAvg = 59,
            xGPerMatch = 2.42f,
            pressingIntensity = "Çok Yüksek (PPDA: 7.8)",
            strengths = listOf(
                "3. bölgede agresif pres ve şok top kazanımları",
                "Osimhen ve Barış Alper ile yüksek hava topu ve fizik üstünlüğü",
                "Mertens & Sara yaratıcı kilit pas üretimi",
                "Duran toplarda etkili stoper katkısı (Davinson Sanchez)"
            ),
            weaknesses = listOf(
                "Yüksek savunma hattı arkasına atılan ani uzun toplar",
                "Beklerin ileride kalması sonrası kanat kontraları"
            ),
            keyPlayers = listOf("Victor Osimhen", "Gabriel Sara", "Lucas Torreira", "Davinson Sanchez"),
            tacticalInsights = "Okan Buruk'un takımı, top rakipteyken 7-8 saniye içinde topu geri kazanmayı hedefler. Osimhen'in stoperleri yıpratması sayesinde Mertens ceza sahası yayında geniş boşluklar bularak şut veya pas açısı üretir.",
            recentFormTrend = "Son 6 maçta 5 galibiyet, maç başına 2.6 gol ortalaması."
        ),
        TeamAnalysis(
            teamId = "fb",
            teamName = "Fenerbahçe",
            shortName = "FB",
            primaryColorHex = 0xFF002D62,
            secondaryColorHex = 0xFFFED100,
            manager = "Jose Mourinho",
            formation = "4-2-3-1 / 3-4-1-2",
            tacticalStyle = "Kompakt Alan Savunması & Hızlı Kanat Akınları",
            summary = "Disiplinli savunma blokları, Saint-Maximin ve Tadic ile kanat varyasyonları ve Dzeko'nun bağlantı oyununa dayalı doğrudan hücumlar.",
            attackRating = 89,
            defenseRating = 86,
            possessionAvg = 56,
            xGPerMatch = 2.18f,
            pressingIntensity = "Orta Blok & Ani Tetikleyici Pres",
            strengths = listOf(
                "Tadic ve Szymanski ile duran top tehdidi",
                "Saint-Maximin ile 1v1 adam eksiltme becerisi",
                "Fred sahadayken yüksek tempo ve top dağıtımı",
                "Dzeko'nun sırtı dönük top saklayıp forvet arkasını kaçırması"
            ),
            weaknesses = listOf(
                "Merkezde Fred olmadığında top çıkarma zorluğu",
                "Bek arkasında bırakılan geçiş boşlukları"
            ),
            keyPlayers = listOf("Fred", "Dusan Tadic", "Edin Dzeko", "Allan Saint-Maximin"),
            tacticalInsights = "Mourinho, rakibin top yapmasına belirli bölgelerde izin verip hata anında Fred ve Tadic üzerinden doğrudan dikine paslarla Saint-Maximin ve Dzeko'yu buluşturmayı kurgular.",
            recentFormTrend = "Ligde son 10 maçın 8'inde ilk golü atan taraf oldu."
        ),
        TeamAnalysis(
            teamId = "bjk",
            teamName = "Beşiktaş",
            shortName = "BJK",
            primaryColorHex = 0xFF111111,
            secondaryColorHex = 0xFFE5E7EB,
            manager = "Giovanni van Bronckhorst",
            formation = "4-3-3",
            tacticalStyle = "Merkez Geçişleri & Dinamik Forvet Koşuları",
            summary = "Rafa Silva'nın driplingleri ve Immobile - Semih Kılıçsoy hücum hattıyla doğrudan skora giden dinamik oyun planı.",
            attackRating = 86,
            defenseRating = 80,
            possessionAvg = 57,
            xGPerMatch = 1.95f,
            pressingIntensity = "Yüksek & İkinci Bölge Baskısı",
            strengths = listOf(
                "Rafa Silva'nın hatlar arası driplingleri ve hızlanması",
                "Semih Kılıçsoy'un ceza sahası içindeki patlayıcı gücü",
                "Gedson Fernandes'in kesintisiz koşu mesafesi ve dinamizmi",
                "Mert Günok'un güven veren kaledeki performansı"
            ),
            weaknesses = listOf(
                "Stoper rotasyonunda fiziksel hava hakimiyeti eksikliği",
                "Öne geçtikten sonra tempoyu soğutma problemleri"
            ),
            keyPlayers = listOf("Rafa Silva", "Semih Kılıçsoy", "Gedson Fernandes", "Ciro Immobile"),
            tacticalInsights = "Top kazanıldığı anda Rafa Silva'ya aktarılır. Portekizli yıldızın dikine sürdüğü toplarda Semih ve kanat oyuncuları çapraz koşularla savunma dengesini bozar.",
            recentFormTrend = "İç sahada yüksek galibiyet yüzdesi, deplasmanlarda kontrollü oyun."
        ),
        TeamAnalysis(
            teamId = "ts",
            teamName = "Trabzonspor",
            shortName = "TS",
            primaryColorHex = 0xFF800020,
            secondaryColorHex = 0xFF0284C7,
            manager = "Şenol Güneş",
            formation = "4-1-4-1",
            tacticalStyle = "Doğrudan Dikine Hücum & Kanat Ortaları",
            summary = "Visca'nın kenar servisleri, Banza'nın kule santrfor özellikleri ve Uğurcan Çakır'ın kurtarış performansıyla sonuca odaklanan yapı.",
            attackRating = 81,
            defenseRating = 79,
            possessionAvg = 52,
            xGPerMatch = 1.65f,
            pressingIntensity = "Orta Blok Kompakt",
            strengths = listOf(
                "Edin Visca'nın nokta atışı ortaları ve tecrübesi",
                "Simon Banza'nın ceza sahasında sıçrama ve hava hakimiyeti",
                "Uğurcan Çakır'ın kritik anlardaki kurtarış yüzdesi"
            ),
            weaknesses = listOf(
                "Orta sahada pas temposunun zaman zaman düşmesi",
                "Geriye düştüğünde oyun kurma zorluğu"
            ),
            keyPlayers = listOf("Simon Banza", "Edin Visca", "Uğurcan Çakır", "Stefan Savic"),
            tacticalInsights = "Şenol Güneş, kenarlardan hızlı servislerle Banza'yı topla buluşturmayı ve ikinci topları ceza sahası çevresinde toplayıp şut tehdidi yaratmayı hedefler.",
            recentFormTrend = "Son haftalarda savunma kurgusunda toparlanma emaresi."
        )
    )
}
