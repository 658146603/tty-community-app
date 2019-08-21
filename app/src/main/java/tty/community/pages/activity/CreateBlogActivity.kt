package tty.community.pages.activity

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.gson.reflect.TypeToken
import kotlinx.android.synthetic.main.activity_create_blog.*
import tty.community.R
import tty.community.adapter.CreateBlogFragmentAdapter
import tty.community.model.*
import tty.community.network.AsyncNetUtils
import tty.community.pages.fragment.CreateBlogShortFragment
import tty.community.util.CONF
import tty.community.util.Message
import java.io.File

class CreateBlogActivity : AppCompatActivity(), View.OnClickListener {
    override fun onClick(v: View?) {
        btn_submit.isEnabled = false
        submit()
        btn_submit.isEnabled = true
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_blog)

        var mode = "text"
        if (intent != null && intent.hasExtra("mode")){
            Log.d(TAG, "mode=${intent.getStringExtra("mode")}")
            mode = intent.getStringExtra("mode")

        }

        setAdapter(mode)
        btn_submit.setOnClickListener(this)
    }

    private fun setAdapter(mode:String) {
        val adapter = CreateBlogFragmentAdapter(supportFragmentManager)
        create_blog_viewPager.adapter = adapter
        create_blog_viewPager.currentItem = when(mode){
            "richText" -> {
                label_edit_type.text = "高级"
                1
            }
            "markdown" -> {
                label_edit_type.text = "Markdown"
                2
            }
            else -> {
                label_edit_type.text = "普通"
                0
            }
        }
    }

    private fun submit(){



        if (create_blog_viewPager.currentItem == 0){
            val tag = Blog.Companion.Tag("000000", "ALL")
            val user = User.find(this)



            if (user != null){
                val blogData = ((create_blog_viewPager.adapter as CreateBlogFragmentAdapter).getItem(create_blog_viewPager.currentItem) as IGetBlogData).getBlogData()
                blogData.title = edit_title.text.toString()
                if (blogData.title.isEmpty()){
                    blogData.title = "####nickname####的动态"
                }
                //create_blog_short_submit.isClickable = false
                Toast.makeText(this, "上传中...", Toast.LENGTH_SHORT).show()
                val json = CONF.gson.toJson(blogData.introduction, object : TypeToken<BlogData.Introduction>(){}.type)
                Log.d(TAG, json)

                // TODO 后台service上传
                AsyncNetUtils.postMultipleForm(CONF.API.blog.create, Params.createBlog(user, blogData.title,
                    when(create_blog_viewPager.currentItem){
                        0-> Blog.Companion.BlogType.Short
                        else-> Blog.Companion.BlogType.Pro
                    }, CONF.gson.toJson(blogData.introduction, object : TypeToken<BlogData.Introduction>(){}.type), blogData.content, tag), blogData.pics, object : AsyncNetUtils.Callback {
                    fun onFail(msg: String): Int {
                        Log.e(CreateBlogShortFragment.TAG, msg)
                        //TODO 备份编辑项目
                        Toast.makeText(this@CreateBlogActivity, msg, Toast.LENGTH_SHORT).show()
                        //create_blog_short_submit.isClickable = true
                        return 1
                    }

                    fun onSuccess(): Int {
                        Toast.makeText(this@CreateBlogActivity, "上传成功", Toast.LENGTH_SHORT).show()
                        finish()
                        return 0
                    }

                    override fun onFailure(msg: String): Int {
                        return onFail(msg)
                    }

                    override fun onResponse(result: String?): Int {
                        val message: Message.MsgData<Blog.Outline>? = Message.MsgData.parse(result, object : TypeToken<Message.MsgData<Blog.Outline>>(){})
                        return if (message != null) {
                            when (message.shortcut) {
                                Shortcut.OK -> onSuccess()
                                Shortcut.TE -> onFail("账号信息已过期，请重新登陆")
                                else -> onFail("shortcut异常")
                            }
                        } else {
                            onFail("解析异常")
                        }
                    }
                })
            } else {
                Toast.makeText(this, "您还未登录，请先登录", Toast.LENGTH_SHORT).show()
            }
        }

    }

    @Deprecated("已经取代掉的API")
    private fun submit(user: User, type: Blog.Companion.BlogType, tag: Blog.Companion.Tag, title: String, introduction: String, content: String, files: ArrayList<File>) {

        Toast.makeText(this, "上传中...", Toast.LENGTH_SHORT).show()

        // TODO 后台service上传
        AsyncNetUtils.postMultipleForm(CONF.API.blog.create, Params.createBlog(user, title, type, introduction, content, tag), files, object : AsyncNetUtils.Callback {
            fun onFail(msg: String): Int {
                Log.e(CreateBlogShortFragment.TAG, msg)
                //TODO 备份编辑项目
                Toast.makeText(this@CreateBlogActivity, msg, Toast.LENGTH_SHORT).show()
                //create_blog_short_submit.isClickable = true
                return 1
            }

            fun onSuccess(): Int {
                Toast.makeText(this@CreateBlogActivity, "上传成功", Toast.LENGTH_SHORT).show()
                finish()
                return 0
            }

            override fun onFailure(msg: String): Int {
                return onFail(msg)
            }

            override fun onResponse(result: String?): Int {
                val message: Message.MsgData<Blog.Outline>? = Message.MsgData.parse(result, object : TypeToken<Message.MsgData<Blog.Outline>>(){})
                return if (message != null) {
                    when (message.shortcut) {
                        Shortcut.OK -> onSuccess()
                        Shortcut.TE -> onFail("账号信息已过期，请重新登陆")
                        else -> onFail("shortcut异常")
                    }
                } else {
                    onFail("解析异常")
                }
            }
        })
    }


    companion object {
        private const val TAG = "CreateBlogActivity"
    }
}
