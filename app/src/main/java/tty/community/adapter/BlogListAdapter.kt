package tty.community.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.gson.reflect.TypeToken
import kotlinx.android.synthetic.main.item_blog_outline.view.*
import tty.community.R
import tty.community.image.BitmapUtil
import tty.community.model.Blog.Outline
import tty.community.model.BlogData
import tty.community.util.CONF
import tty.community.util.Time
import tty.community.util.Time.getFormattedTime
import tty.community.widget.RoundAngleImageView
import java.lang.Exception

class BlogListAdapter : RecyclerView.Adapter<BlogListAdapter.ViewHolder>() {
    private var blogs = ArrayList<Outline>()

    private var mItemClickListener: OnItemClickListener? = null

    private lateinit var context: Context

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        context = parent.context
        return ViewHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.item_blog_outline,
                parent,
                false
            ), mItemClickListener
        )
    }

    override fun getItemCount(): Int {
        return blogs.size
    }


    interface OnItemClickListener : View.OnClickListener {
        fun onItemClick(v: View?, position: Int, blog: Outline)
    }

    fun setOnItemClickListener(listener: OnItemClickListener) {
        this.mItemClickListener = listener
    }

    fun add(blogs: ArrayList<Outline>) {
        this.blogs.addAll(blogs)
        notifyDataSetChanged()
    }

    fun init(blogs: ArrayList<Outline>) {
        this.blogs = blogs
        notifyDataSetChanged()
    }

    fun getLastBlogId(): String? {
        if (blogs.isEmpty()) {
            return null
        }
        return blogs[blogs.size - 1].blogId
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val author = blogs[position].author
        val introduction = blogs[position].introduction
        val blogId = blogs[position].blogId
        try {
            val data: BlogData.Introduction = CONF.gson.fromJson(introduction, object : TypeToken<BlogData.Introduction>(){}.type)
            holder.introduction.text = data.summary
            if (data.pics.size > 0){
                val picture = BlogData.toUrl(blogId, data.pics[0])
                holder.picture.visibility = View.VISIBLE
                Glide.with(context).load(picture).apply(BitmapUtil.optionsMemoryCache()).centerCrop().into(holder.picture)
            } else {
                holder.picture.visibility = View.GONE
            }
        } catch (e:Exception){

            val summary = introduction.substringAfter("****summary****").substringBefore("****metadata****").trim()
            holder.introduction.text = "(v0)\n$summary"
            val picture = CONF.API.blog.picture + "?" + introduction.substringAfter("****metadata****").substringBefore("****end****").trim()

            if (picture.isNotEmpty() && picture.contains("key")) {
                holder.picture.visibility = View.VISIBLE
                Glide.with(context).load(picture).apply(BitmapUtil.optionsMemoryCache()).centerCrop().into(holder.picture)
            } else {
                holder.picture.visibility = View.GONE
            }
        }


        holder.time.text = Time.getTime(blogs[position].lastActiveTime).getFormattedTime()
        holder.title.text = blogs[position].title
        holder.nickname.text = blogs[position].nickname

        holder.tag.text = blogs[position].tag
        val portrait = blogs[position].portrait()


        Glide.with(context).load(portrait).apply(BitmapUtil.optionsMemoryCache()).centerCrop().into(holder.portrait)
    }

    inner class ViewHolder(v: View, private var listener: OnItemClickListener?) :
        RecyclerView.ViewHolder(v), View.OnClickListener {
        private var mListener: OnItemClickListener
        private val card: CardView = v.blog_outline_card
        val time: TextView = v.blog_time
        val title: TextView = v.blog_title
        val nickname: TextView = v.blog_author_nickname
        val introduction: TextView = v.blog_introduction
        val portrait: RoundAngleImageView = v.blog_author_portrait
        val tag: TextView = v.blog_tag
        val picture: RoundAngleImageView = v.blog_picture
        private val more: Button = v.blog_outline_more

        override fun onClick(p0: View?) {
            listener?.onItemClick(p0, layoutPosition, blogs[layoutPosition])
        }

        init {
            card.setOnClickListener(this)
            more.setOnClickListener(this)
            tag.setOnClickListener(this)
            portrait.setOnClickListener(this)
            picture.setOnClickListener(this)
            mListener = this.listener!!
        }
    }
}