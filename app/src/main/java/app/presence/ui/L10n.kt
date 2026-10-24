package app.presence.ui

import java.util.Locale

fun resolveLang(pref: String): String {
    if (pref != "system") return pref
    val code = Locale.getDefault().language
    return if (code in setOf("tr", "es", "pt", "fr", "ja", "ko")) code else "en"
}

class T(lang: String) {
    private val i = when (lang) {
        "tr" -> 1
        "es" -> 2
        "pt" -> 3
        "fr" -> 4
        "ja" -> 5
        "ko" -> 6
        else -> 0
    }

    private fun s(en: String, tr: String, es: String, pt: String, fr: String, ja: String, ko: String): String =
        arrayOf(en, tr, es, pt, fr, ja, ko)[i]

    val timer = s("Timer", "Zamanlayıcı", "Temporizador", "Temporizador", "Minuteur", "タイマー", "타이머")
    val projects = s("Projects", "Projeler", "Proyectos", "Projetos", "Projets", "プロジェクト", "프로젝트")
    val stats = s("Stats", "İstatistik", "Datos", "Dados", "Stats", "記録", "기록")
    val settings = s("Settings", "Ayarlar", "Ajustes", "Ajustes", "Réglages", "設定", "설정")
    val focus = s("Focus", "Odak", "Enfoque", "Foco", "Focus", "集中", "집중")
    val shortRest = s("Short rest", "Kısa mola", "Descanso corto", "Pausa curta", "Pause courte", "小休止", "짧은 쉼")
    val longRest = s("Long rest", "Uzun mola", "Descanso largo", "Pausa longa", "Pause longue", "長休止", "긴 쉼")
    val track = s("Track", "Takip", "Registro", "Registo", "Suivi", "計測", "기록")
    val startFocus = s("Start focus", "Odağı başlat", "Empezar enfoque", "Começar foco", "Lancer le focus", "集中を始める", "집중 시작")
    val pause = s("Pause", "Duraklat", "Pausa", "Pausar", "Pause", "一時停止", "일시정지")
    val resume = s("Resume", "Sürdür", "Seguir", "Retomar", "Reprendre", "再開", "계속")
    val stop = s("Stop", "Bitir", "Parar", "Parar", "Arrêter", "終了", "종료")
    val skip = s("Skip", "Geç", "Saltar", "Saltar", "Passer", "スキップ", "건너뛰기")
    val startNext = s("Start next", "Sonrakini başlat", "Empezar siguiente", "Começar a seguir", "Lancer la suite", "次を始める", "다음 시작")
    val dismiss = s("Dismiss", "Kapat", "Cerrar", "Fechar", "Fermer", "閉じる", "닫기")
    val trackTime = s("Track time", "Süre tut", "Registrar tiempo", "Registar tempo", "Compter le temps", "時間を計る", "시간 재기")
    val save = s("Save", "Kaydet", "Guardar", "Guardar", "Enregistrer", "保存", "저장")
    val cancel = s("Cancel", "Vazgeç", "Cancelar", "Cancelar", "Annuler", "キャンセル", "취소")
    val delete = s("Delete", "Sil", "Eliminar", "Apagar", "Supprimer", "削除", "삭제")
    val back = s("Back", "Geri", "Volver", "Voltar", "Retour", "戻る", "뒤로")
    val remaining = s("Time left", "Kalan", "Tiempo restante", "Tempo restante", "Temps restant", "残り", "남은 시간")
    val paused = s("Paused", "Duraklatıldı", "En pausa", "Em pausa", "En pause", "停止中", "일시정지됨")
    val finished = s("Stretch finished", "Dilim bitti", "Tramo terminado", "Trecho terminado", "Passage terminé", "一区切り", "한 구간 끝")
    val ready = s("{phase} is ready", "{phase} hazır", "{phase} listo", "{phase} pronto", "{phase} est prêt", "{phase}の番です", "{phase} 준비됨")
    val waiting = s("Ready when you are", "Hazır olunca", "Cuando quieras", "Quando quiser", "Quand vous voulez", "好きなときに", "준비되면")
    val cycle = s("Cycle", "Döngü", "Ciclo", "Ciclo", "Cycle", "周期", "주기")
    val switchTitle = s("Switch timer?", "Zamanlayıcı değişsin mi?", "¿Cambiar el temporizador?", "Mudar o temporizador?", "Changer le minuteur ?", "タイマーを切り替えますか？", "타이머를 바꿀까요?")
    val switchBody = s("The current stretch will be saved.", "Şu anki dilim kaydedilir.", "El tramo actual se guardará.", "O trecho atual será guardado.", "Le passage en cours sera enregistré.", "今の区間は保存されます。", "지금 구간은 저장됩니다.")
    val notificationsOff = s("Notifications are off. The chime still plays when a stretch ends.", "Bildirimler kapalı. Dilim bitince ses yine çalar.", "Las notificaciones están apagadas. El aviso sonoro sigue al terminar.", "As notificações estão desligadas. O aviso sonoro continua no fim.", "Les notifications sont coupées. Le carillon sonne encore à la fin.", "通知はオフです。終わりの音は鳴ります。", "알림이 꺼져 있습니다. 끝날 때 소리는 납니다.")
    val emptyProjects = s("No projects yet.", "Henüz proje yok.", "Aún no hay proyectos.", "Ainda não há projetos.", "Pas encore de projet.", "プロジェクトはまだありません。", "프로젝트가 없습니다.")
    val newProject = s("New project", "Yeni proje", "Nuevo proyecto", "Novo projeto", "Nouveau projet", "新しいプロジェクト", "새 프로젝트")
    val chooseProject = s("Choose a project.", "Bir proje seçin.", "Elige un proyecto.", "Escolha um projeto.", "Choisissez un projet.", "プロジェクトを選んでください。", "프로젝트를 고르세요.")
    val today = s("Today", "Bugün", "Hoy", "Hoje", "Aujourd’hui", "今日", "오늘")
    val moveUp = s("Move up", "Yukarı", "Subir", "Subir", "Monter", "上へ", "위로")
    val moveDown = s("Move down", "Aşağı", "Bajar", "Descer", "Descendre", "下へ", "아래로")
    val deleteProject = s("Delete this project?", "Bu proje silinsin mi?", "¿Eliminar este proyecto?", "Apagar este projeto?", "Supprimer ce projet ?", "このプロジェクトを削除しますか？", "이 프로젝트를 삭제할까요?")
    val deleteProjectBody = s("Tasks go with it. Time already kept stays in your history.", "Görevler de gider. Tutulan süre geçmişte kalır.", "Las tareas se van con él. El tiempo guardado sigue en el historial.", "As tarefas vão junto. O tempo guardado fica no histórico.", "Les tâches partent avec lui. Le temps déjà gardé reste.", "タスクも消えます。記録した時間は残ります。", "할 일도 함께 사라집니다. 기록한 시간은 남습니다.")
    val deleteTask = s("Delete this task?", "Bu görev silinsin mi?", "¿Eliminar esta tarea?", "Apagar esta tarefa?", "Supprimer cette tâche ?", "このタスクを削除しますか？", "이 할 일을 삭제할까요?")
    val deleteTaskBody = s("The time already kept stays.", "Tutulan süre kalır.", "El tiempo guardado se queda.", "O tempo guardado fica.", "Le temps déjà gardé reste.", "記録した時間は残ります。", "기록한 시간은 남습니다.")
    val tasks = s("Tasks", "Görevler", "Tareas", "Tarefas", "Tâches", "タスク", "할 일")
    val newTask = s("New task", "Yeni görev", "Nueva tarea", "Nova tarefa", "Nouvelle tâche", "新しいタスク", "새 할 일")
    val emptyTasks = s("No tasks yet.", "Henüz görev yok.", "Aún no hay tareas.", "Ainda não há tarefas.", "Pas encore de tâche.", "タスクはまだありません。", "할 일이 없습니다.")
    val goal = s("Goal", "Hedef", "Meta", "Meta", "Objectif", "目標", "목표")
    val setGoal = s("Set a goal", "Hedef koy", "Poner una meta", "Definir uma meta", "Fixer un objectif", "目標を置く", "목표 정하기")
    val daily = s("Today’s target", "Bugünün hedefi", "Meta de hoy", "Meta de hoje", "Cible du jour", "今日の目標", "오늘의 목표")
    val remainder = s("Still to do", "Kalan", "Pendiente", "Ainda falta", "Encore à faire", "残り", "남은 양")
    val deadline = s("Deadline", "Son gün", "Fecha límite", "Prazo", "Échéance", "期限", "마감")
    val clearGoal = s("Clear goal", "Hedefi kaldır", "Quitar meta", "Limpar meta", "Retirer l’objectif", "目標を消す", "목표 지우기")
    val noGoal = s("No goal on this project.", "Bu projede hedef yok.", "Este proyecto no tiene meta.", "Este projeto não tem meta.", "Pas d’objectif sur ce projet.", "目標はありません。", "목표가 없습니다.")
    val tags = s("Tags", "Etiketler", "Etiquetas", "Etiquetas", "Étiquettes", "タグ", "태그")
    val newTag = s("New tag", "Yeni etiket", "Nueva etiqueta", "Nova etiqueta", "Nouvelle étiquette", "新しいタグ", "새 태그")
    val all = s("All", "Hepsi", "Todas", "Todas", "Toutes", "すべて", "전체")
    val newGroup = s("New group", "Yeni grup", "Nuevo grupo", "Novo grupo", "Nouveau groupe", "新しいグループ", "새 그룹")
    val rename = s("Rename", "Yeniden adlandır", "Renombrar", "Renomear", "Renommer", "名前を変える", "이름 바꾸기")
    val deleteGroup = s("Delete this group?", "Bu grup silinsin mi?", "¿Eliminar este grupo?", "Apagar este grupo?", "Supprimer ce groupe ?", "このグループを削除しますか？", "이 그룹을 삭제할까요?")
    val deleteGroupBody = s("Its projects move to Personal.", "Projeleri Kişisel grubuna geçer.", "Sus proyectos pasan a Personal.", "Os projetos passam para Personal.", "Ses projets passent dans Personal.", "プロジェクトは Personal に移ります。", "프로젝트는 Personal로 옮깁니다.")
    val locked = s("Premium", "Premium", "Premium", "Premium", "Premium", "プレミアム", "프리미엄")
    val name = s("Name", "Ad", "Nombre", "Nome", "Nom", "名前", "이름")
    val giveName = s("Give it a name.", "Bir ad verin.", "Ponle un nombre.", "Dê um nome.", "Donnez-lui un nom.", "名前をつけてください。", "이름을 지어 주세요.")
    val ownTimer = s("Lengths for this project", "Bu projeye özel süreler", "Duraciones de este proyecto", "Durações deste projeto", "Durées de ce projet", "このプロジェクトの長さ", "이 프로젝트의 길이")
    val hours = s("Hours", "Saat", "Horas", "Horas", "Heures", "時間", "시간")
    val minutes = s("Minutes", "Dakika", "Minutos", "Minutos", "Minutes", "分", "분")
    val week = s("Week", "Hafta", "Semana", "Semana", "Semaine", "週", "주")
    val month = s("Month", "Ay", "Mes", "Mês", "Mois", "月", "월")
    val year = s("Year", "Yıl", "Año", "Ano", "Année", "年", "연")
    val emptyRange = s("Nothing kept in this range.", "Bu aralıkta kayıt yok.", "Nada guardado en este tramo.", "Nada guardado neste intervalo.", "Rien sur cette période.", "この範囲には記録がありません。", "이 범위에는 기록이 없습니다.")
    val share = s("Share", "Paylaş", "Compartir", "Partilhar", "Partager", "共有", "공유")
    val manual = s("Add a past stretch", "Geçmiş dilim ekle", "Añadir un tramo pasado", "Adicionar um trecho passado", "Ajouter un passage passé", "過去の区間を足す", "지난 구간 추가")
    val charts = s("Charts, the calendar, and sharing", "Grafikler, takvim ve paylaşma", "Gráficos, calendario y compartir", "Gráficos, calendário e partilha", "Graphiques, calendrier et partage", "グラフ、カレンダー、共有", "차트, 달력, 공유")
    val future = s("You can add a stretch for today or an earlier day.", "Bugün ya da daha önceki bir gün için ekleyebilirsiniz.", "Puedes añadir un tramo de hoy o de un día anterior.", "Pode adicionar um trecho de hoje ou de um dia anterior.", "Vous pouvez ajouter un passage d’aujourd’hui ou d’avant.", "今日かそれより前の日に足せます。", "오늘이나 그 이전 날에 추가할 수 있습니다.")
    val overlap = s("That overlaps the timer that is running.", "Bu, çalışan zamanlayıcıyla çakışıyor.", "Eso se solapa con el temporizador en marcha.", "Isso sobrepõe o temporizador em curso.", "Cela chevauche le minuteur en cours.", "動いているタイマーと重なります。", "실행 중인 타이머와 겹칩니다.")
    val unlabeled = s("No project", "Projesiz", "Sin proyecto", "Sem projeto", "Sans projet", "プロジェクトなし", "프로젝트 없음")
    val openPresence = s("Open Presence", "Presence’i aç", "Abrir Presence", "Abrir Presence", "Ouvrir Presence", "Presence を開く", "Presence 열기")
    val widgetToday = s("Today", "Bugün", "Hoy", "Hoje", "Aujourd’hui", "今日", "오늘")
    val testerTitle = s("Tester switch", "Deneme anahtarı", "Interruptor de prueba", "Interruptor de teste", "Interrupteur d’essai", "試用スイッチ", "시험 스위치")
    val testerBody = s("Turn premium on or off. This does not buy anything.", "Premium’u açın ya da kapatın. Bu bir satın alma değildir.", "Activa o desactiva premium. Esto no es una compra.", "Ligue ou desligue o premium. Isto não é uma compra.", "Active ou coupez le premium. Ce n’est pas un achat.", "プレミアムのオンとオフです。購入ではありません。", "프리미엄을 켜거나 끕니다. 결제가 아닙니다.")
    val premiumOn = s("Premium is on", "Premium açık", "Premium activado", "Premium ligado", "Premium activé", "プレミアムはオン", "프리미엄 켜짐")
    val premiumOff = s("Free mode", "Ücretsiz kip", "Modo gratuito", "Modo gratuito", "Mode gratuit", "無料モード", "무료 모드")
    val openSwitch = s("Open tester switch", "Deneme anahtarını aç", "Abrir el interruptor", "Abrir o interruptor", "Ouvrir l’interrupteur", "試用スイッチを開く", "시험 스위치 열기")
    val appearance = s("Appearance", "Görünüm", "Apariencia", "Aparência", "Apparence", "外観", "모양")
    val system = s("System", "Sistem", "Sistema", "Sistema", "Système", "システム", "시스템")
    val light = s("Light", "Açık", "Claro", "Claro", "Clair", "ライト", "밝게")
    val dark = s("Dark", "Koyu", "Oscuro", "Escuro", "Sombre", "ダーク", "어둡게")
    val language = s("Language", "Dil", "Idioma", "Idioma", "Langue", "言語", "언어")
    val textSize = s("Text size", "Yazı boyutu", "Tamaño del texto", "Tamanho do texto", "Taille du texte", "文字サイズ", "글자 크기")
    val compact = s("Compact", "Sıkı", "Compacto", "Compacto", "Compact", "コンパクト", "콤팩트")
    val standard = s("Standard", "Standart", "Estándar", "Padrão", "Standard", "標準", "표준")
    val large = s("Large", "Büyük", "Grande", "Grande", "Grand", "大", "크게")
    val dayStarts = s("Day starts at", "Gün başlangıcı", "El día empieza a las", "O dia começa às", "Le jour commence à", "一日の始まり", "하루의 시작")
    val alarm = s("Alarm", "Alarm", "Alarma", "Alarme", "Alarme", "アラーム", "알람")
    val chime = s("Chime", "Çan", "Campana", "Sino", "Carillon", "チャイム", "차임")
    val bell = s("Bell", "Zil", "Campana grave", "Sino grave", "Cloche", "ベル", "벨")
    val pulse = s("Pulse", "Vuruş", "Pulso", "Pulso", "Pulsation", "パルス", "펄스")
    val custom = s("From this phone", "Bu telefondan", "De este teléfono", "Deste telefone", "Depuis ce téléphone", "この端末から", "이 휴대전화에서")
    val background = s("Background", "Arka plan", "Fondo", "Fundo", "Fond", "背景音", "배경음")
    val off = s("Off", "Kapalı", "Apagado", "Desligado", "Aucun", "オフ", "끄기")
    val rain = s("Rain", "Yağmur", "Lluvia", "Chuva", "Pluie", "雨", "비")
    val brook = s("Brook", "Dere", "Arroyo", "Riacho", "Ruisseau", "せせらぎ", "시내")
    val hearth = s("Hearth", "Ocak", "Hogar", "Lareira", "Âtre", "炉端", "화로")
    val volume = s("Volume", "Ses", "Volumen", "Volume", "Volume", "音量", "음량")
    val countUp = s("Count up", "Yukarı say", "Cuenta ascendente", "Contagem crescente", "Compte croissant", "カウントアップ", "올라가는 시계")
    val countDown = s("Count down", "Geri say", "Cuenta atrás", "Contagem decrescente", "Compte à rebours", "カウントダウン", "내려가는 시계")
    val autoContinue = s("Continue on its own", "Kendiliğinden sürsün", "Seguir solo", "Continuar sozinho", "Enchaîner seul", "自動で次へ", "저절로 이어가기")
    val focusLen = s("Focus length", "Odak süresi", "Duración del enfoque", "Duração do foco", "Durée du focus", "集中の長さ", "집중 길이")
    val shortLen = s("Short rest", "Kısa mola", "Descanso corto", "Pausa curta", "Pause courte", "小休止", "짧은 쉼")
    val longLen = s("Long rest", "Uzun mola", "Descanso largo", "Pausa longa", "Pause longue", "長休止", "긴 쉼")
    val cycles = s("Focuses before a long rest", "Uzun moladan önce odak", "Enfoques antes del descanso largo", "Focos antes da pausa longa", "Focus avant une pause longue", "長休止までの集中", "긴 쉼 전의 집중")
    val exportJson = s("Save a backup", "Yedek kaydet", "Guardar una copia", "Guardar uma cópia", "Enregistrer une copie", "控えを保存", "백업 저장")
    val exportCsv = s("Save a CSV", "CSV kaydet", "Guardar un CSV", "Guardar um CSV", "Enregistrer un CSV", "CSV を保存", "CSV 저장")
    val importJson = s("Bring in a backup", "Yedek getir", "Traer una copia", "Trazer uma cópia", "Importer une copie", "控えを読み込む", "백업 가져오기")
    val badFile = s("That file isn’t a Presence backup.", "Bu dosya bir Presence yedeği değil.", "Ese archivo no es una copia de Presence.", "Esse arquivo não é uma cópia do Presence.", "Ce fichier n’est pas une copie Presence.", "Presence の控えではありません。", "Presence 백업이 아닙니다.")
    val imported = s("Brought in.", "Getirildi.", "Traído.", "Trazido.", "Importé.", "読み込みました。", "가져왔습니다.")
    val pair = s("Pair on Wi-Fi", "Wi-Fi’de eşle", "Emparejar por Wi-Fi", "Emparelhar no Wi-Fi", "Apparier en Wi-Fi", "Wi-Fi でペア", "Wi-Fi로 짝짓기")
    val devices = s("Paired phones", "Eşlenen telefonlar", "Teléfonos emparejados", "Telefones emparelhados", "Téléphones appariés", "ペア済みの端末", "짝지은 휴대전화")
    val about = s("On this phone", "Bu telefonda", "En este teléfono", "Neste telefone", "Sur ce téléphone", "この端末で", "이 휴대전화에서")
    val aboutBody = s("Presence keeps projects, tasks, and sessions on this phone. There is no account.", "Presence proje, görev ve oturumları bu telefonda tutar. Hesap yoktur.", "Presence guarda proyectos, tareas y sesiones en este teléfono. No hay cuenta.", "O Presence guarda projetos, tarefas e sessões neste telefone. Não há conta.", "Presence garde projets, tâches et sessions sur ce téléphone. Il n’y a pas de compte.", "プロジェクト、タスク、記録はこの端末にあります。アカウントはありません。", "프로젝트, 할 일, 기록은 이 휴대전화에 있습니다. 계정은 없습니다.")
    val showCode = s("Show code", "Kodu göster", "Mostrar código", "Mostrar código", "Afficher le code", "コードを表示", "코드 보이기")
    val enterCode = s("Enter code", "Kodu gir", "Introducir código", "Introduzir código", "Entrer le code", "コードを入力", "코드 입력")
    val pairAction = s("Pair", "Eşle", "Emparejar", "Emparelhar", "Apparier", "ペアにする", "짝짓기")
    val expires = s("Expires in", "Bitmesine", "Caduca en", "Expira em", "Expire dans", "残り", "만료까지")
    val expired = s("That code has expired. Ask for a new one.", "Bu kodun süresi doldu. Yenisini isteyin.", "Ese código ha caducado. Pide uno nuevo.", "Esse código expirou. Peça um novo.", "Ce code a expiré. Demandez-en un nouveau.", "このコードは期限切れです。新しいものを頼んでください。", "코드가 만료되었습니다. 새 코드를 받으세요.")
    val wrongCode = s("That code doesn’t match.", "Bu kod uymuyor.", "Ese código no coincide.", "Esse código não confere.", "Ce code ne correspond pas.", "コードが違います。", "코드가 맞지 않습니다.")
    val noCode = s("No other Presence is showing a code on this Wi-Fi.", "Bu Wi-Fi’de kod gösteren başka Presence yok.", "Ningún otro Presence muestra un código en este Wi-Fi.", "Nenhum outro Presence mostra um código neste Wi-Fi.", "Aucun autre Presence n’affiche de code sur ce Wi-Fi.", "この Wi-Fi でコードを出している Presence はありません。", "이 Wi-Fi에서 코드를 보여주는 다른 Presence가 없습니다.")
    val left = s("The other Presence left before this finished.", "Diğer Presence bitmeden ayrıldı.", "El otro Presence se fue antes de terminar.", "O outro Presence saiu antes de terminar.", "L’autre Presence est parti avant la fin.", "相手の Presence が途中でいなくなりました。", "다른 Presence가 끝나기 전에 떠났습니다.")
    val dropped = s("Wi-Fi dropped. Nothing was lost. Try again on the same Wi-Fi.", "Wi-Fi koptu. Bir şey kaybolmadı. Aynı Wi-Fi’de yeniden deneyin.", "Se cayó el Wi-Fi. No se perdió nada. Prueba otra vez en la misma red.", "O Wi-Fi caiu. Nada se perdeu. Tente de novo na mesma rede.", "Le Wi-Fi a lâché. Rien n’a été perdu. Réessayez sur le même Wi-Fi.", "Wi-Fi が切れました。データは残っています。同じ Wi-Fi でもう一度。", "Wi-Fi가 끊겼습니다. 잃은 것은 없습니다. 같은 Wi-Fi에서 다시 시도하세요.")
    val selfPhone = s("That’s this phone.", "Bu, bu telefon.", "Ese es este teléfono.", "Esse é este telefone.", "C’est ce téléphone.", "これはこの端末です。", "이 휴대전화입니다.")
    val pairedOk = s("Paired. You’ll sync on this Wi-Fi from now on.", "Eşlendi. Bundan sonra bu Wi-Fi’de eşitlenir.", "Emparejado. A partir de ahora se sincroniza en este Wi-Fi.", "Emparelhado. A partir de agora sincroniza neste Wi-Fi.", "Apparié. La synchro se fera sur ce Wi-Fi.", "ペアになりました。これからはこの Wi-Fi で同期します。", "짝이 되었습니다. 이제 이 Wi-Fi에서 동기화합니다.")
    val syncNow = s("Sync now", "Şimdi eşitle", "Sincronizar ahora", "Sincronizar agora", "Synchroniser", "今すぐ同期", "지금 동기화")
    val forget = s("Forget", "Unut", "Olvidar", "Esquecer", "Oublier", "忘れる", "잊기")
    val notVisible = s("Can’t see that Presence on this Wi-Fi.", "Bu Wi-Fi’de o Presence görünmüyor.", "No se ve ese Presence en este Wi-Fi.", "Não se vê esse Presence neste Wi-Fi.", "Ce Presence n’est pas visible sur ce Wi-Fi.", "この Wi-Fi にその Presence が見えません。", "이 Wi-Fi에서 그 Presence가 보이지 않습니다.")
    val noPeers = s("No paired phones yet.", "Henüz eşlenen telefon yok.", "Aún no hay teléfonos emparejados.", "Ainda não há telefones emparelhados.", "Pas encore de téléphone apparié.", "ペア済みの端末はまだありません。", "아직 짝지은 휴대전화가 없습니다.")
    val syncing = s("Looking for the other Presence…", "Diğer Presence aranıyor…", "Buscando el otro Presence…", "À procura do outro Presence…", "Recherche de l’autre Presence…", "相手の Presence を探しています…", "다른 Presence를 찾는 중…")
    val synced = s("Synced.", "Eşitlendi.", "Sincronizado.", "Sincronizado.", "Synchronisé.", "同期しました。", "동기화했습니다.")
    val offerHelp = s("On the other phone, choose Enter code.", "Diğer telefonda Kodu gir’i seçin.", "En el otro teléfono, elige Introducir código.", "No outro telefone, escolha Introduzir código.", "Sur l’autre téléphone, choisissez Entrer le code.", "もう一方でコードを入力を選んでください。", "다른 휴대전화에서 코드 입력을 고르세요.")
    val enterHelp = s("Type the four digits from the other phone.", "Diğer telefondaki dört rakamı yazın.", "Escribe los cuatro dígitos del otro teléfono.", "Escreva os quatro dígitos do outro telefone.", "Tapez les quatre chiffres de l’autre téléphone.", "もう一方の四桁を入力してください。", "다른 휴대전화의 네 자리를 입력하세요.")
    val newCode = s("Show a new code", "Yeni kod göster", "Mostrar un código nuevo", "Mostrar um código novo", "Afficher un nouveau code", "新しいコード", "새 코드 보이기")
    val premiumIntro = s("This is part of premium. Use the tester switch in Settings to try it. Nothing is purchased.", "Bu, premium’un parçası. Denemek için Ayarlar’daki anahtarı kullanın. Satın alma yok.", "Esto es parte de premium. Usa el interruptor de Ajustes para probarlo. No hay compra.", "Isto faz parte do premium. Use o interruptor em Ajustes para experimentar. Não há compra.", "Ceci fait partie du premium. Utilisez l’interrupteur dans Réglages pour l’essayer. Rien n’est acheté.", "これはプレミアムです。設定の試用スイッチで試せます。購入はありません。", "이것은 프리미엄입니다. 설정의 시험 스위치로 써 보세요. 결제는 없습니다.")
    val atLeast = s("Use at least one minute.", "En az bir dakika.", "Usa al menos un minuto.", "Use pelo menos um minuto.", "Une minute au minimum.", "1分以上にしてください。", "최소 1분이어야 합니다.")
    val direction = s("Direction", "Yön", "Dirección", "Direção", "Sens", "向き", "방향")
    val data = s("Files", "Dosyalar", "Archivos", "Ficheiros", "Fichiers", "ファイル", "파일")
    val sounds = s("Sounds", "Sesler", "Sonidos", "Sons", "Sons", "音", "소리")
    val timerSettings = s("Timer", "Zamanlayıcı", "Temporizador", "Temporizador", "Minuteur", "タイマー", "타이머")
    val message = s("Note", "Not", "Nota", "Nota", "Note", "メモ", "메모")
    val todos = s("Todos", "Yapılacaklar", "Pendientes", "Afazeres", "À faire", "やること", "할 일")
    val addItem = s("Add", "Ekle", "Añadir", "Adicionar", "Ajouter", "追加", "추가")
    val emptyTodos = s("Nothing on the list.", "Listede bir şey yok.", "No hay nada en la lista.", "Nada na lista.", "Rien sur la liste.", "リストは空です。", "목록이 비어 있습니다.")
    val todoHint = s("Add a todo", "Yapılacak ekle", "Añadir un pendiente", "Adicionar um afazer", "Ajouter une tâche", "やることを追加", "할 일 추가")
    val reminders = s("Reminders", "Anımsatmalar", "Recordatorios", "Lembretes", "Rappels", "リマインダー", "알림")
    val dailyReminder = s("Daily reminder", "Günlük anımsatma", "Recordatorio diario", "Lembrete diário", "Rappel du jour", "毎日のリマインダー", "매일 알림")
    val streakReminder = s("Streak reminder", "Seri anımsatması", "Recordatorio de racha", "Lembrete de sequência", "Rappel de série", "連続のリマインダー", "연속 알림")
    val timeToFocus = s("Time to focus.", "Odaklanma zamanı.", "Hora de enfocarte.", "Hora de focar.", "C’est l’heure de se concentrer.", "集中の時間です。", "집중할 시간입니다.")
    val followSystem = s("Follow system", "Sistemi izle", "Seguir el sistema", "Seguir o sistema", "Suivre le système", "システムに合わせる", "시스템 따르기")
    val start = s("Start", "Başlat", "Empezar", "Começar", "Commencer", "開始", "시작")
    val streak = s("Streak", "Seri", "Racha", "Sequência", "Série", "連続", "연속")

    fun streakLine(count: Int, logged: Boolean): String = when {
        count <= 0 -> s("No streak yet", "Henüz seri yok", "Aún no hay racha", "Ainda não há sequência", "Pas encore de série", "まだ連続はありません", "아직 연속이 없습니다")
        logged -> s("$count days, with today", "$count gün, bugün dahil", "$count días, con hoy", "$count dias, com hoje", "$count jours, avec aujourd’hui", "$count 日、今日を含む", "${count}일, 오늘 포함")
        else -> s("$count days. Today is still open.", "$count gün. Bugün henüz açık.", "$count días. Hoy sigue abierto.", "$count dias. Hoje ainda está aberto.", "$count jours. Aujourd’hui est encore ouvert.", "$count 日。今日はまだこれから。", "${count}일. 오늘은 아직 열려 있습니다.")
    }

    fun cycleLine(done: Int, total: Int): String = s(
        "Focus $done of $total",
        "Odak $done / $total",
        "Enfoque $done de $total",
        "Foco $done de $total",
        "Focus $done sur $total",
        "集中 $done / $total",
        "집중 $done / $total",
    )

    fun expiresIn(clock: String): String = s(
        "Expires in $clock",
        "$clock içinde biter",
        "Caduca en $clock",
        "Expira em $clock",
        "Expire dans $clock",
        "あと $clock",
        "$clock 후 만료",
    )

    fun pairMessage(code: String): String = when (code) {
        "dropped" -> dropped
        "expired" -> expired
        "wrong_code" -> wrongCode
        "no_code" -> noCode
        "left" -> left
        "self" -> selfPhone
        "paired" -> pairedOk
        "syncing" -> syncing
        "not_visible" -> notVisible
        "synced" -> synced
        "no_token" -> notVisible
        else -> ""
    }
}
