import android.os.Parcelable
import com.dev.james.sayariproject.models.iss.Crew
import kotlinx.parcelize.Parcelize

@Parcelize
data class ActiveExpedition(
    val crew: List<Crew>,
    val end: String?,
    val id: Int,
    val name: String,
    val start: String,
    val url: String
):Parcelable