package com.kai.crawler

import android.util.Log
import android.util.SparseBooleanArray
import com.kai.common.utils.LogUtils
import com.kai.common.utils.StringUtils
import com.kai.crawler.entity.book.SearchBook
import com.kai.crawler.entity.chapter.Chapter
import com.kai.crawler.entity.source.SourceManager
import com.kai.crawler.xpath.exception.XpathSyntaxErrorException
import com.kai.crawler.xpath.model.JXDocument
import com.kai.crawler.xpath.model.JXNode
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.schedulers.Schedulers
import org.jsoup.Jsoup
import java.net.URI
import java.net.URISyntaxException
import java.net.URLEncoder

//Xpath  匹配规则
//https://blog.csdn.net/weixin_44462294/article/details/104410755
class Crawler {
    companion object {
        const val TAG = "Crawler"


        fun search(keyword: String): Observable<List<SearchBook>> {
           return Observable.create<List<SearchBook>> { emitter ->
               val checkedMap: SparseBooleanArray = SourceManager.getSourceEnableSparseArray()
               for (i in 0.until(checkedMap.size())) {
                   val id = SourceManager.CONFIGS.keyAt(i)
                   val config = SourceManager.CONFIGS.valueAt(i)
                   val source = SourceManager.SOURCES.get(id)
                   if (!checkedMap.get(id)) {
                       continue
                   }
                   val resource = ArrayList<JXNode?>()
                   var url = ""
                   try {
                       url = if (config.search?.charset.isNullOrEmpty()) {
                           String.format(
                                   source.searchUrl, URLEncoder.encode(
                                   keyword,
                                   config.search?.charset
                           )
                           )
                       } else {
                           String.format(source.searchUrl, keyword)
                       }
                       LogUtils.e(TAG, "url = $url")
                       val connect = Jsoup.connect(url).get()
                       val jxDocument = JXDocument(connect)
                       resource.clear()
                       val selN = jxDocument.selN(config.search?.xpath)
                       selN.let {
                           resource.addAll(it)
                       }
                   } catch (e: Exception) {
                       LogUtils.e(TAG, "search getConnect error $e")
                       continue
                   }

                   if (resource == null) {
                       continue
                   }

                   val books: ArrayList<SearchBook> = ArrayList()
                   try {
                       for (jxNode in resource) {
                           var bookLink = ""
                           jxNode?.let { node ->
                               config.search?.let { search ->
                                   val book = SearchBook()
                                   search.coverXpath?.let { coverPath ->
                                       val cover = getNodeStr(node, coverPath)
                                       cover?.let { coverUrl ->
                                           val urlVerification = urlVerification(coverUrl, url)
                                           urlVerification?.let {
                                               book.cover = it
                                               LogUtils.e(TAG, "search $keyword cover = ${book.cover}")
                                           }
                                       }
                                   }

                                   search.titleXpath?.let { titleXPath ->
                                       val title = getNodeStr(jxNode, titleXPath)
                                       title?.let {
                                           book.title = it
                                           LogUtils.e(TAG, "search $keyword title = ${book.title}")
                                       }
                                   }



                                   search.linkXpath?.let { linkXPath ->
                                       val link = getNodeStr(jxNode, linkXPath)
                                       link?.let { linkPath ->
                                           val urlVerification = urlVerification(linkPath, url)
                                           urlVerification?.let {
                                               //储存书源到map中
                                               bookLink = it
                                               book.sources.add(SearchBook.SL(it, source))
                                               LogUtils.e(TAG, "search $keyword link = $it")
                                           }
                                       }
                                   }


                                   search.authorXpath?.let { authorXPath ->
                                       val author = getNodeStr(jxNode, authorXPath)
                                       author?.let {
                                           book.author = it
                                           LogUtils.e(TAG, "search $keyword author = ${book.author}")
                                       }
                                   }


                                   search.descXpath?.let { describerXPath ->
                                       val describer = getNodeStr(jxNode, describerXPath)
                                       describer?.let {
                                           book.descriptor = describer
                                           LogUtils.e(
                                                   TAG,
                                                   "search $keyword descriptor = ${book.descriptor}"
                                           )
                                       }
                                   }


                                   if (bookLink.isNotEmpty()) {
                                       books.add(book)
                                   }

                               }
                           }
                       }
                       emitter.onNext(books)
                       emitter.onComplete()
                   } catch (e: Exception) {
                       emitter.onError(e)
                       LogUtils.e(TAG, e.toString())
                   }
               }

            }.subscribeOn(Schedulers.newThread()).observeOn(AndroidSchedulers.mainThread())
            //循环遍历完全部资源，搜索完毕

        }


        fun catalog(sl: SearchBook.SL): Observable<List<Chapter>> {
            return Observable.create{ emitter ->
                emitter
                if (sl == null || sl.source == null || sl.link.isEmpty()) {
                    emitter.onComplete()
                }
                val sourceId = sl.source.id
                val config = SourceManager.CONFIGS[sourceId]
                val source = SourceManager.SOURCES[sourceId]
                if (config.catalog == null) {
                    emitter.onComplete()
                }


                val rs: ArrayList<JXNode?> = ArrayList()
                try {
                    val jxDocument = JXDocument(Jsoup.connect(sl.link).get())
                    config?.let { sourceConfig ->
                        val catalog = sourceConfig.catalog
                        catalog?.let { bookCatalog ->
                            val xpath = bookCatalog.xpath
                            rs.clear()
                            val selN = jxDocument.selN(xpath)
                            rs.addAll(selN)
                        }
                    }
                } catch (e: java.lang.Exception) {
                    LogUtils.e(TAG, "parse catalog error $e")
                }
                if (rs.isEmpty()) {
                    emitter.onComplete()
                }
                val chapters: ArrayList<Chapter> = ArrayList()
                try {
                    for (jxNode in rs) {
                        config?.let { sourceConfig ->
                            sourceConfig.catalog?.let { bookCatalog ->
                                val chapter = Chapter()
                                bookCatalog.linkXpath?.let {
                                    if (it.isNotEmpty()) {
                                        jxNode?.let { node ->
                                            val link = getNodeStr(node, it)
                                            if (link != null && link.isNotEmpty()) {
                                                val chapterLink = urlVerification(link, sl.link)
                                                chapterLink?.let {
                                                    chapter.link = chapterLink
                                                    LogUtils.e(
                                                            TAG,
                                                            "parse chapter link is $chapterLink"
                                                    )
                                                }
                                            }
                                        }

                                    }
                                }


                                bookCatalog.titleXpath?.let {
                                    jxNode?.let { node ->
                                        val nodeStr = getNodeStr(node, it)
                                        nodeStr?.let {
                                            chapter.title = it
                                            LogUtils.e(TAG, "parse chapter title is ${chapter.title}")
                                        }
                                    }
                                }
                                chapters.add(chapter)

                            }
                        }
                    }
                    emitter.onNext(chapters)
                    emitter.onComplete()
                } catch (e: java.lang.Exception) {
                    emitter.onError(e)
                    LogUtils.e(TAG, "parse catalog error is $e")
                }
            }

        }



        fun content(sl: SearchBook.SL, url: String){
            LogUtils.e(TAG, "parse content url = $url")
            if(sl == null || sl.link == null || sl.link.isEmpty() || url.isEmpty()){
                return
            }
            val sourceId = sl.source.id
            val config = SourceManager.CONFIGS[sourceId]
            if (config.content == null) {
                return
            }
            try {
                val link = urlVerification(url, sl.link)
                LogUtils.e(TAG, "parse content link =   $link")
                val jxDocument =
                    JXDocument(Jsoup.connect(link).get())
                var content = getNodeStr(jxDocument, config.content!!.xpath!!)
                val builder = java.lang.StringBuilder()
                val lines = content!!.split(" ".toRegex()).toTypedArray()
                for (line in lines) {
                     StringUtils.trim(line)?.let {
                         if (it.isNotEmpty()) {
                             builder.append("        ").append(line).append("\n")
                         }
                     }

                }
                content = builder.toString()
                LogUtils.e(TAG, "parse content =$content")
            } catch (e: java.lang.Exception) {
                LogUtils.e(TAG, "parse content error is $e")
            }
        }

        /**
         * 获取 通过xpath 查找到的字符串
         *
         * @param startNode 只有JXDocument   和  JXNode 两种
         * @param xpath
         * @return
         */
        private fun getNodeStr(startNode: Any, xpath: String): String? {
            val rs = StringBuilder()
            try {
                val list: List<*>? = when (startNode) {
                    is JXDocument -> {
                        startNode.sel(xpath)
                    }
                    is JXNode -> {
                        startNode.sel(xpath)
                    }
                    else -> {
                        return ""
                    }
                }
                for (node in list!!) {
                    rs.append(node.toString())
                }
            } catch (e: XpathSyntaxErrorException) {
                Log.e(TAG, e.toString())
            }
            return rs.toString()
        }


        /**
         * des 将链接转换为可访问的完整链接
         * @param link 待转换链接
         * @param linkWithHost 源网站链接
         * @return 可访问的完整链接
         */
        @Throws(URISyntaxException::class)
        private fun urlVerification(link: String, linkWithHost: String): String? {
            var link = link
            var linkWithHost = linkWithHost
            if (link.isEmpty()) {
                return link
            }
            if (link.startsWith("/")) {
                val original = URI(linkWithHost)
                val uri = URI(original.scheme, original.authority, link, null)
                link = uri.toString()
            } else if (!link.startsWith("http://") && !link.startsWith("https://")) {
                if (linkWithHost.endsWith("html") || linkWithHost.endsWith("htm")) {
                    linkWithHost = linkWithHost.substring(0, linkWithHost.lastIndexOf("/") + 1)
                } else if (!linkWithHost.endsWith("/")) {
                    linkWithHost = "$linkWithHost/"
                }
                link = linkWithHost + link
            }
            return link
        }
    }
}