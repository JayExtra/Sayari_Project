import com.dev.james.sayariproject.models.iss.Crew

data class ActiveExpedition(
    val crew: List<Crew>,
    val end: Any,
    val id: Int,
    val name: String,
    val start: String,
    val url: String
)