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
    val play = s("Play", "Oynat", "Reproducir", "Reproduzir", "Lecture", "再生", "재생")
    val resume = s("Resume", "Sürdür", "Seguir", "Retomar", "Reprendre", "再開", "계속")
    val stop = s("Stop", "Bitir", "Parar", "Parar", "Arrêter", "終了", "종료")
    val skip = s("Skip", "Geç", "Saltar", "Saltar", "Passer", "スキップ", "건너뛰기")
    val skipHelp = s("Ends this stretch and moves to the next one.", "Bu dilimi bitirir ve sonrakine geçer.", "Termina este tramo y pasa al siguiente.", "Termina este trecho e passa ao seguinte.", "Termine ce passage et passe au suivant.", "この区間を終えて次へ進みます。", "이 구간을 끝내고 다음으로 갑니다.")
    val shortNowHelp = s("Starts a short rest now.", "Kısa molayı şimdi başlatır.", "Empieza un descanso corto ahora.", "Começa uma pausa curta agora.", "Lance une pause courte maintenant.", "今すぐ小休止を始めます。", "지금 짧은 쉼을 시작합니다.")
    val longNowHelp = s("Starts a long rest now.", "Uzun molayı şimdi başlatır.", "Empieza un descanso largo ahora.", "Começa uma pausa longa agora.", "Lance une pause longue maintenant.", "今すぐ長休止を始めます。", "지금 긴 쉼을 시작합니다.")
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
    val noGroup = s("No group", "Grup yok", "Sin grupo", "Sem grupo", "Aucun groupe", "グループなし", "그룹 없음")
    val groups = s("Group", "Grup", "Grupo", "Grupo", "Groupe", "グループ", "그룹")
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
    val seconds = s("Seconds", "Saniye", "Segundos", "Segundos", "Secondes", "秒", "초")
    val workTime = s("Work time", "Çalışma süresi", "Tiempo de trabajo", "Tempo de trabalho", "Temps de travail", "作業時間", "작업 시간")
    val sessionStart = s("Start time", "Başlangıç saati", "Hora de inicio", "Hora de início", "Heure de début", "開始時刻", "시작 시간")
    val recordedOverlap = s("This overlaps time already logged.", "Bu süre kayıtlı süreyle çakışıyor.", "Se solapa con tiempo ya registrado.", "Sobrepõe tempo já registado.", "Cela chevauche du temps déjà enregistré.", "記録済みの時間と重なります。", "이미 기록한 시간과 겹칩니다.")
    val loggedTime = s("Logged", "Kaydedilen", "Registrado", "Registado", "Enregistré", "記録", "기록")
    val pomodoroPreset = s("4 × 25 min · 5 min breaks", "4 × 25 dk · 5 dk mola", "4 × 25 min · pausas de 5 min", "4 × 25 min · pausas de 5 min", "4 × 25 min · pauses de 5 min", "25分 × 4 · 休憩5分", "25분 × 4 · 5분 휴식")
    val workHelp = s(
        "Total focus time for one plan, split into equal stretches. Breaks are extra.",
        "Projenin tümü. Eşit dilimlere bölünür.",
        "El proyecto entero. Se reparte en tramos iguales.",
        "O projeto inteiro. Divide-se em trechos iguais.",
        "Tout le projet. Il se partage en passages égaux.",
        "プロジェクト全体。等しい区間に分かれます。",
        "프로젝트 전체. 같은 길이의 구간으로 나뉩니다.",
    )
    val shortBreaks = s("Short breaks", "Kısa molalar", "Descansos cortos", "Pausas curtas", "Pauses courtes", "小休止", "짧은 쉼")
    val longBreaks = s("Long breaks", "Uzun molalar", "Descansos largos", "Pausas longas", "Pauses longues", "長休止", "긴 쉼")
    val shortCountHelp = s(
        "How many of the gaps are a short break.",
        "Aralıklardan kaçı kısa mola.",
        "Cuántos huecos son un descanso corto.",
        "Quantos intervalos são uma pausa curta.",
        "Combien d’intervalles sont une pause courte.",
        "隙間のうち短い休止の数。",
        "사이 중 짧은 쉼의 수.",
    )
    val longCountHelp = s(
        "How many of the gaps are a long break. They are spaced out.",
        "Aralıklardan kaçı uzun mola. Aralara yayılır.",
        "Cuántos huecos son un descanso largo. Se reparten.",
        "Quantos intervalos são uma pausa longa. Ficam espaçados.",
        "Combien d’intervalles sont une pause longue. Elles sont espacées.",
        "隙間のうち長い休止の数。均等に置きます。",
        "사이 중 긴 쉼의 수. 고르게 둡니다.",
    )
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
    val fileError = s("Could not read or save the file. Please try another location.", "Dosya okunamadı veya kaydedilemedi. Başka bir konum deneyin.", "No se pudo leer o guardar el archivo. Prueba otra ubicación.", "Não foi possível ler ou guardar o ficheiro. Tente outro local.", "Impossible de lire ou enregistrer le fichier. Essayez un autre emplacement.", "ファイルを読み書きできません。別の保存先を試してください。", "파일을 읽거나 저장할 수 없습니다. 다른 위치를 선택하세요.")
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
    val focusHelp = s("Minutes in one focus stretch.", "Bir odak diliminin dakikası.", "Minutos de un tramo de enfoque.", "Minutos de um trecho de foco.", "Minutes d’un passage de focus.", "一区切りの集中の分数。", "한 구간 집중의 분.")
    val shortHelp = s("Minutes in a short rest.", "Kısa molanın dakikası.", "Minutos del descanso corto.", "Minutos da pausa curta.", "Minutes de la pause courte.", "小休止の分数。", "짧은 쉼의 분.")
    val longHelp = s("Minutes in a long rest.", "Uzun molanın dakikası.", "Minutos del descanso largo.", "Minutos da pausa longa.", "Minutes de la pause longue.", "長休止の分数。", "긴 쉼의 분.")
    val cyclesHelp = s("How many focuses come before a long rest.", "Uzun moladan önce kaç odak gelir.", "Cuántos enfoques van antes del descanso largo.", "Quantos focos vêm antes da pausa longa.", "Combien de focus précèdent une pause longue.", "長休止の前に何回集中するか。", "긴 쉼 전에 집중이 몇 번인지.")
    val countUpHelp = s("On counts up from zero. Off counts down.", "Açıkken sıfırdan yukarı sayar. Kapalıyken geri sayar.", "Encendido cuenta desde cero. Apagado cuenta atrás.", "Ligado conta a partir do zero. Desligado conta para trás.", "Activé, ça compte depuis zéro. Coupé, ça compte à rebours.", "オンはゼロから上へ。オフは残りを数えます。", "켜면 0부터 올라갑니다. 끄면 내려갑니다.")
    val autoHelp = s("The next stretch starts when this one ends.", "Bu dilim bitince sonraki başlar.", "El siguiente tramo empieza cuando este termina.", "O trecho seguinte começa quando este acaba.", "Le passage suivant commence à la fin de celui-ci.", "この区間が終わると次が始まります。", "이 구간이 끝나면 다음이 시작됩니다.")
    val dayHelp = s("Hours after midnight when a new day begins.", "Yeni günün gece yarısından kaç saat sonra başladığı.", "Horas después de medianoche en que empieza el día.", "Horas depois da meia-noite em que o dia começa.", "Heures après minuit où le jour commence.", "真夜中から何時間後に一日が始まるか。", "자정 이후 몇 시간 뒤에 하루가 시작되는지.")
    val dailyHelp = s("A nudge at this time, once a day.", "Her gün bu saatte bir anımsatma.", "Un aviso a esta hora, una vez al día.", "Um aviso a esta hora, uma vez por dia.", "Un rappel à cette heure, une fois par jour.", "毎日この時刻に一度。", "매일 이 시각에 한 번.")
    val streakHelp = s("A nudge at this time if today has no focus yet.", "Bugün henüz odak yoksa bu saatte bir anımsatma.", "Un aviso a esta hora si hoy aún no hay enfoque.", "Um aviso a esta hora se hoje ainda não há foco.", "Un rappel à cette heure s’il n’y a pas encore de focus aujourd’hui.", "今日まだ集中がなければ、この時刻に一度。", "오늘 아직 집중이 없으면 이 시각에 한 번.")
    val lightHelp = s("A light background with dark text.", "Açık zemin, koyu yazı.", "Fondo claro con texto oscuro.", "Fundo claro com texto escuro.", "Fond clair, texte foncé.", "明るい背景に暗い文字。", "밝은 바탕에 어두운 글자.")
    val darkHelp = s("A dark background with light text.", "Koyu zemin, açık yazı.", "Fondo oscuro con texto claro.", "Fundo escuro com texto claro.", "Fond sombre, texte clair.", "暗い背景に明るい文字。", "어두운 바탕에 밝은 글자.")
    val followHelp = s("Match this phone’s light or dark setting.", "Telefonun açık ya da koyu ayarını izle.", "Igual que el ajuste claro u oscuro del teléfono.", "Igual à definição clara ou escura do telefone.", "Suivre le réglage clair ou sombre du téléphone.", "端末のライトかダークに合わせる。", "휴대전화의 밝게 또는 어둡게를 따릅니다.")
    val compactHelp = s("Slightly smaller text.", "Biraz daha küçük yazı.", "Texto un poco más pequeño.", "Texto um pouco menor.", "Texte un peu plus petit.", "少し小さい文字。", "조금 더 작은 글자.")
    val standardHelp = s("The usual text size.", "Olağan yazı boyutu.", "El tamaño de siempre.", "O tamanho habitual.", "La taille habituelle.", "いつもの文字サイズ。", "보통 글자 크기.")
    val largeHelp = s("Larger text.", "Daha büyük yazı.", "Texto más grande.", "Texto maior.", "Texte plus grand.", "大きい文字。", "더 큰 글자.")
    val langSystemHelp = s("Match the phone’s language.", "Telefonun dilini kullan.", "Usar el idioma del teléfono.", "Usar o idioma do telefone.", "Utiliser la langue du téléphone.", "端末の言語に合わせる。", "휴대전화의 언어를 따릅니다.")
    val langEnglish = s("English", "English", "English", "English", "English", "English", "English")
    val langEnHelp = s("Show Presence in English.", "Presence’i İngilizce göster.", "Mostrar Presence en inglés.", "Mostrar o Presence em inglês.", "Afficher Presence en anglais.", "Presence を英語で表示。", "Presence를 영어로 표시.")
    val langTurkish = s("Türkçe", "Türkçe", "Türkçe", "Türkçe", "Türkçe", "Türkçe", "Türkçe")
    val langTrHelp = s("Show Presence in Turkish.", "Presence’i Türkçe göster.", "Mostrar Presence en turco.", "Mostrar o Presence em turco.", "Afficher Presence en turc.", "Presence をトルコ語で表示。", "Presence를 터키어로 표시.")
    val langSpanish = s("Español", "Español", "Español", "Español", "Español", "Español", "Español")
    val langEsHelp = s("Show Presence in Spanish.", "Presence’i İspanyolca göster.", "Mostrar Presence en español.", "Mostrar o Presence em espanhol.", "Afficher Presence en espagnol.", "Presence をスペイン語で表示。", "Presence를 스페인어로 표시.")
    val langPortuguese = s("Português", "Português", "Português", "Português", "Português", "Português", "Português")
    val langPtHelp = s("Show Presence in Portuguese.", "Presence’i Portekizce göster.", "Mostrar Presence en portugués.", "Mostrar o Presence em português.", "Afficher Presence en portugais.", "Presence をポルトガル語で表示。", "Presence를 포르투갈어로 표시.")
    val langFrench = s("Français", "Français", "Français", "Français", "Français", "Français", "Français")
    val langFrHelp = s("Show Presence in French.", "Presence’i Fransızca göster.", "Mostrar Presence en francés.", "Mostrar o Presence em francês.", "Afficher Presence en français.", "Presence をフランス語で表示。", "Presence를 프랑스어로 표시.")
    val langJapanese = s("日本語", "日本語", "日本語", "日本語", "日本語", "日本語", "日本語")
    val langJaHelp = s("Show Presence in Japanese.", "Presence’i Japonca göster.", "Mostrar Presence en japonés.", "Mostrar o Presence em japonês.", "Afficher Presence en japonais.", "Presence を日本語で表示。", "Presence를 일본어로 표시.")
    val langKorean = s("한국어", "한국어", "한국어", "한국어", "한국어", "한국어", "한국어")
    val langKoHelp = s("Show Presence in Korean.", "Presence’i Korece göster.", "Mostrar Presence en coreano.", "Mostrar o Presence em coreano.", "Afficher Presence en coréen.", "Presence を韓国語で表示。", "Presence를 한국어로 표시.")
    val chimeHelp = s("A short bright tone.", "Kısa ve parlak bir ses.", "Un tono breve y brillante.", "Um tom breve e claro.", "Un ton bref et clair.", "短く明るい音。", "짧고 밝은 소리.")
    val bellHelp = s("A lower ring.", "Daha tok bir zil.", "Un tañido más grave.", "Um toque mais grave.", "Un tintement plus grave.", "低めの鈴。", "낮은 종소리.")
    val pulseHelp = s("A soft beat.", "Yumuşak bir vuruş.", "Un pulso suave.", "Uma batida suave.", "Une pulsation douce.", "やわらかい拍。", "부드러운 박자.")
    val customHelp = s("A sound file from this phone.", "Bu telefondaki bir ses dosyası.", "Un archivo de sonido de este teléfono.", "Um ficheiro de som deste telefone.", "Un fichier son de ce téléphone.", "この端末の音声ファイル。", "이 휴대전화의 소리 파일.")
    val offHelp = s("No background sound.", "Arka plan sesi yok.", "Sin sonido de fondo.", "Sem som de fundo.", "Pas de son de fond.", "背景音なし。", "배경음 없음.")
    val rainHelp = s("Steady rain.", "Sürekli yağmur.", "Lluvia constante.", "Chuva contínua.", "Pluie continue.", "途切れない雨。", "계속 내리는 비.")
    val brookHelp = s("Running water.", "Akan su.", "Agua que corre.", "Água a correr.", "Eau qui coule.", "流れる水。", "흐르는 물.")
    val hearthHelp = s("A low fire.", "Alçak bir ateş.", "Un fuego bajo.", "Um fogo baixo.", "Un feu bas.", "低い火。", "낮은 불.")
    val volumeHelp = s("How loud the background sound is.", "Arka plan sesinin yüksekliği.", "Lo alto que suena el fondo.", "O volume do som de fundo.", "Le volume du son de fond.", "背景音の大きさ。", "배경음이 얼마나 큰지.")
    val exportJsonHelp = s("Write projects and sessions to a file.", "Proje ve oturumları bir dosyaya yaz.", "Escribe proyectos y sesiones en un archivo.", "Grava projetos e sessões num ficheiro.", "Écrit projets et sessions dans un fichier.", "プロジェクトと記録をファイルに書く。", "프로젝트와 기록을 파일로 씁니다.")
    val exportCsvHelp = s("Write sessions as a spreadsheet.", "Oturumları bir tablo olarak yaz.", "Escribe las sesiones como una hoja de cálculo.", "Grava as sessões como uma folha de cálculo.", "Écrit les sessions dans un tableur.", "記録を表にする。", "기록을 표로 씁니다.")
    val importHelp = s("Read a Presence file back onto this phone.", "Bir Presence dosyasını bu telefona geri oku.", "Vuelve a leer un archivo de Presence en este teléfono.", "Lê de novo um ficheiro Presence neste telefone.", "Relit un fichier Presence sur ce téléphone.", "Presence のファイルをこの端末に読み戻す。", "Presence 파일을 이 휴대전화로 다시 읽습니다.")
    val pairHelp = s("Link another phone on the same Wi-Fi.", "Aynı Wi-Fi’deki başka bir telefonu bağla.", "Enlaza otro teléfono en el mismo Wi-Fi.", "Liga outro telefone no mesmo Wi-Fi.", "Relie un autre téléphone sur le même Wi-Fi.", "同じ Wi-Fi の別の端末をつなぐ。", "같은 Wi-Fi의 다른 휴대전화를 연결합니다.")
    val start = s("Start", "Başlat", "Empezar", "Começar", "Commencer", "開始", "시작")
    val decrease = s("Decrease", "Azalt", "Bajar", "Diminuir", "Diminuer", "減らす", "줄이기")
    val increase = s("Increase", "Artır", "Subir", "Aumentar", "Augmenter", "増やす", "늘리기")
    val focusModeHelp = s(
        "Runs a focus, then a short rest, and a long rest after the set number of focuses.",
        "Bir odak, sonra kısa mola, ayarlanan sayıda odaktan sonra uzun mola.",
        "Un enfoque, luego un descanso corto y un descanso largo tras los enfoques fijados.",
        "Um foco, depois uma pausa curta e uma pausa longa após os focos definidos.",
        "Un focus, puis une pause courte, et une pause longue après le nombre de focus choisi.",
        "集中のあと小休止。決めた回数のあとに長休止。",
        "집중 다음 짧은 쉼, 정한 횟수 뒤에 긴 쉼.",
    )
    val trackModeHelp = s(
        "Counts open time on the project. It does not start a rest.",
        "Projedeki açık süreyi sayar. Mola başlatmaz.",
        "Cuenta el tiempo abierto del proyecto. No empieza un descanso.",
        "Conta o tempo aberto no projeto. Não começa uma pausa.",
        "Compte le temps ouvert sur le projet. N’ouvre pas de pause.",
        "プロジェクトの時間を数えます。休止は始まりません。",
        "프로젝트의 열린 시간을 셉니다. 쉼은 시작하지 않습니다.",
    )
    val startShort = s("Start short rest", "Kısa molayı başlat", "Empezar descanso corto", "Começar pausa curta", "Lancer la pause courte", "小休止を始める", "짧은 쉼 시작")
    val startLong = s("Start long rest", "Uzun molayı başlat", "Empezar descanso largo", "Começar pausa longa", "Lancer la pause longue", "長休止を始める", "긴 쉼 시작")
    val resetSettings = s("Reset settings", "Ayarları sıfırla", "Restablecer ajustes", "Repor ajustes", "Réinitialiser", "設定を戻す", "설정 되돌리기")
    val resetHelp = s(
        "Restores durations, theme, day start, and reminder times. Projects, todos, and logged sessions stay.",
        "Süreleri, temayı, gün başlangıcını ve anımsatma saatlerini geri getirir. Proje, yapılacak ve kayıtlı oturumlar kalır.",
        "Devuelve duraciones, tema, inicio del día y horas de recordatorio. Proyectos, pendientes y sesiones se quedan.",
        "Repõe durações, tema, início do dia e horas dos lembretes. Projetos, afazeres e sessões ficam.",
        "Rend les durées, le thème, le début du jour et les heures de rappel. Projets, tâches et sessions restent.",
        "長さ、テーマ、一日の始まり、リマインダーの時刻を戻します。プロジェクトと記録は残ります。",
        "길이, 테마, 하루의 시작, 알림 시각을 되돌립니다. 프로젝트, 할 일, 기록은 남습니다.",
    )
    val resetDone = s("Settings restored.", "Ayarlar geri geldi.", "Ajustes restablecidos.", "Ajustes repostos.", "Réglages rendus.", "設定を戻しました。", "설정을 되돌렸습니다.")
    val colorTheme = s("Color theme", "Renk teması", "Tema de color", "Tema de cor", "Thème de couleur", "色のテーマ", "색 테마")
    val paletteGrove = s("Grove", "Grove", "Grove", "Grove", "Grove", "Grove", "Grove")
    val paletteGroveHelp = s("Green, the usual Presence colors.", "Yeşil, Presence’in olağan renkleri.", "Verde, los colores de siempre.", "Verde, as cores de sempre.", "Vert, les couleurs habituelles.", "緑。いつもの色。", "초록, 늘 쓰던 색.")
    val paletteHarbor = s("Harbor", "Harbor", "Harbor", "Harbor", "Harbor", "Harbor", "Harbor")
    val paletteHarborHelp = s("A blue theme.", "Mavi bir tema.", "Un tema azul.", "Um tema azul.", "Un thème bleu.", "青のテーマ。", "파란 테마.")
    val paletteDusk = s("Dusk", "Dusk", "Dusk", "Dusk", "Dusk", "Dusk", "Dusk")
    val paletteDuskHelp = s("A purple theme.", "Mor bir tema.", "Un tema morado.", "Um tema roxo.", "Un thème violet.", "紫のテーマ。", "보라 테마.")
    val paletteSlate = s("Slate", "Slate", "Slate", "Slate", "Slate", "Slate", "Slate")
    val paletteSlateHelp = s("A cool gray theme.", "Soğuk gri bir tema.", "Un tema gris frío.", "Um tema cinza frio.", "Un thème gris froid.", "涼しい灰色のテーマ。", "차가운 회색 테마.")
    val paletteBerry = s("Berry", "Berry", "Berry", "Berry", "Berry", "Berry", "Berry")
    val paletteBerryHelp = s("A deep red theme.", "Koyu kırmızı bir tema.", "Un tema rojo oscuro.", "Um tema vermelho escuro.", "Un thème rouge profond.", "深い赤のテーマ。", "짙은 붉은 테마.")
    val paletteAmber = s("Amber", "Amber", "Amber", "Amber", "Amber", "Amber", "Amber")
    val paletteAmberHelp = s("A warm gold theme.", "Sıcak altın bir tema.", "Un tema dorado cálido.", "Um tema dourado quente.", "Un thème or chaud.", "暖かい金のテーマ。", "따뜻한 금색 테마.")
    val paletteMoss = s("Moss", "Moss", "Moss", "Moss", "Moss", "Moss", "Moss")
    val paletteMossHelp = s("A yellow-green theme.", "Sarı yeşil bir tema.", "Un tema verde amarillo.", "Um tema verde-amarelo.", "Un thème vert jaune.", "黄緑のテーマ。", "노란 초록 테마.")
    val paletteIris = s("Iris", "Iris", "Iris", "Iris", "Iris", "Iris", "Iris")
    val paletteIrisHelp = s("A blue-violet theme.", "Mavi mor bir tema.", "Un tema azul violeta.", "Um tema azul-violeta.", "Un thème bleu violet.", "青紫のテーマ。", "푸른 보라 테마.")
    val paletteCopper = s("Copper", "Copper", "Copper", "Copper", "Copper", "Copper", "Copper")
    val paletteCopperHelp = s("A burnt orange theme.", "Yanık turuncu bir tema.", "Un tema naranja tostado.", "Um tema laranja queimado.", "Un thème orange brûlé.", "焦げた橙のテーマ。", "그을린 주황 테마.")
    val paletteTide = s("Tide", "Tide", "Tide", "Tide", "Tide", "Tide", "Tide")
    val paletteTideHelp = s("A teal theme.", "Deniz yeşili bir tema.", "Un tema verde azulado.", "Um tema verde-azulado.", "Un thème sarcelle.", "青緑のテーマ。", "청록 테마.")
    val paletteInk = s("Ink", "Ink", "Ink", "Ink", "Ink", "Ink", "Ink")
    val paletteInkHelp = s("A deep navy theme.", "Koyu lacivert bir tema.", "Un tema azul marino.", "Um tema azul-marinho.", "Un thème bleu marine.", "濃い紺のテーマ。", "짙은 남색 테마.")
    val themeMenuHelp = s("Light, dark, or follow this phone.", "Açık, koyu ya da telefonu izle.", "Claro, oscuro o seguir el teléfono.", "Claro, escuro ou seguir o telefone.", "Clair, sombre, ou suivre le téléphone.", "ライト、ダーク、または端末に合わせる。", "밝게, 어둡게, 또는 휴대전화를 따릅니다.")
    val paletteMenuHelp = s("A named set of colors that stays readable.", "Okunaklı kalan adlı bir renk takımı.", "Un conjunto de colores con nombre, legible.", "Um conjunto de cores com nome, legível.", "Un jeu de couleurs nommé, lisible.", "読みやすい名前付きの色。", "읽기 쉬운 이름의 색 묶음.")
    val textMenuHelp = s("How large the text is.", "Yazının büyüklüğü.", "El tamaño del texto.", "O tamanho do texto.", "La taille du texte.", "文字の大きさ。", "글자 크기.")
    val languageMenuHelp = s("The language Presence uses.", "Presence’in kullandığı dil.", "El idioma de Presence.", "O idioma do Presence.", "La langue de Presence.", "Presence の言語。", "Presence가 쓰는 언어.")
    val alarmMenuHelp = s("The sound when a stretch ends.", "Bir dilim bitince çalan ses.", "El sonido al terminar un tramo.", "O som quando um trecho acaba.", "Le son à la fin d’un passage.", "区間が終わるときの音。", "구간이 끝날 때의 소리.")
    val backgroundMenuHelp = s("A sound during focus or tracking.", "Odak ya da takip sırasında bir ses.", "Un sonido durante el enfoque o el registro.", "Um som durante o foco ou o registo.", "Un son pendant le focus ou le suivi.", "集中か計測のあいだの音。", "집중이나 기록 중의 소리.")
    val sdCard = s("SD card", "SD card", "SD card", "SD card", "SD card", "SD card", "SD card")
    val sdNone = s("This phone has no SD card.", "Bu telefonda SD kart yok.", "Este teléfono no tiene tarjeta SD.", "Este telefone não tem cartão SD.", "Ce téléphone n’a pas de carte SD.", "この端末に SD カードはありません。", "이 휴대전화에는 SD 카드가 없습니다.")
    val sdPresent = s("An SD card is in this phone.", "Bu telefonda SD kart var.", "Hay una tarjeta SD en este teléfono.", "Há um cartão SD neste telefone.", "Une carte SD est dans ce téléphone.", "この端末に SD カードがあります。", "이 휴대전화에 SD 카드가 있습니다.")
    val sdOnCard = s("An SD card is in this phone, and the database is on it.", "SD kart telefonda ve veritabanı kartta.", "Hay una tarjeta SD y la base de datos está en ella.", "Há um cartão SD e a base de dados está nele.", "Une carte SD est là, et la base est dessus.", "SD カードがあり、データベースはそこにあります。", "SD 카드가 있고 데이터베이스는 그 카드에 있습니다.")
    val sdMissing = s("The SD card with this data is not in the phone.", "Verinin olduğu SD kart telefonda değil.", "La tarjeta SD con estos datos no está en el teléfono.", "O cartão SD com estes dados não está no telefone.", "La carte SD qui porte ces données n’est pas dans le téléphone.", "このデータがある SD カードは端末にありません。", "이 데이터가 있는 SD 카드가 휴대전화에 없습니다.")
    val sdMove = s("Store data on the SD card", "Veriyi SD karta taşı", "Guardar los datos en la tarjeta SD", "Guardar os dados no cartão SD", "Mettre les données sur la carte SD", "データを SD カードに置く", "데이터를 SD 카드에 두기")
    val sdMoveHelp = s("Moves the existing database onto the card.", "Var olan veritabanını karta taşır.", "Mueve la base de datos que ya existe a la tarjeta.", "Move a base de dados existente para o cartão.", "Déplace la base déjà là sur la carte.", "今のデータベースをカードへ移します。", "있는 데이터베이스를 카드로 옮깁니다.")
    val sdInternal = s("Store data on this phone", "Veriyi bu telefonda tut", "Guardar los datos en este teléfono", "Guardar os dados neste telefone", "Mettre les données sur ce téléphone", "データをこの端末に置く", "데이터를 이 휴대전화에 두기")
    val sdInternalHelp = s("Moves the database back onto this phone.", "Veritabanını bu telefona geri taşır.", "Vuelve a mover la base de datos a este teléfono.", "Move a base de dados de volta para este telefone.", "Ramène la base sur ce téléphone.", "データベースをこの端末に戻します。", "데이터베이스를 이 휴대전화로 되돌립니다.")
    val sdFailed = s("The database stayed where it was.", "Veritabanı yerinde kaldı.", "La base de datos se quedó donde estaba.", "A base de dados ficou onde estava.", "La base est restée où elle était.", "データベースはそのままです。", "데이터베이스는 그대로입니다.")
    val sdCheck = s("Check again", "Yeniden bak", "Comprobar otra vez", "Verificar de novo", "Vérifier encore", "もう一度見る", "다시 확인")
    val streak = s("Streak", "Seri", "Racha", "Sequência", "Série", "連続", "연속")

    fun stretchSummary(count: Int, each: String) = s(
        "$count stretches · $each each",
        "$count dilim · her biri $each",
        "$count tramos · $each cada uno",
        "$count trechos · $each cada",
        "$count passages · $each chacun",
        "$count 区間 · それぞれ $each",
        "$count 구간 · 각각 $each",
    )

    fun pauses(count: Int, time: String) = s(
        "$count pauses · $time",
        "$count duraklama · $time",
        "$count pausas · $time",
        "$count pausas · $time",
        "$count pauses · $time",
        "一時停止 $count · $time",
        "일시정지 $count · $time",
    )

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
