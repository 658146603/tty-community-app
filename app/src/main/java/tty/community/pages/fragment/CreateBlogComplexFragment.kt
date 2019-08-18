package tty.community.pages.fragment


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import tty.community.R
import tty.community.model.Blog.Companion.BlogType

class CreateBlogComplexFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_create_blog_complex, container, false)
    }

    companion object {
        const val TAG = "CreateBlogComplexFragment"
        private val TYPE = BlogType.Pro
    }

}
