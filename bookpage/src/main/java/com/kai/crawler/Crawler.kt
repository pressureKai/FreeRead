package com.kai.crawler

import android.util.Log
import android.util.SparseBooleanArray
import com.kai.common.utils.LogUtils
import com.kai.common.utils.StringUtils
import com.kai.bookpage.model.BookRecommend
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
import java.lang.NullPointerException
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
                val books: ArrayList<SearchBook> = ArrayList()
                var connectError = false
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
                        connectError = true
                        continue
                    }

                    if (resource == null) {
                        continue
                    }


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
                                                LogUtils.e(
                                                    TAG,
                                                    "search $keyword cover = ${book.cover}"
                                                )
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
                                            LogUtils.e(
                                                TAG,
                                                "search $keyword author = ${book.author}"
                                            )
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
                //循环遍历完全部资源，搜索完毕

                if (books.size == 0 && connectError) {
                    LogUtils.e(TAG, "search finish")
                    emitter.onError(Exception())
                }

            }.subscribeOn(Schedulers.newThread()).observeOn(AndroidSchedulers.mainThread())


        }


        fun catalog(sl: SearchBook.SL): Observable<List<Chapter>> {
            return Observable.create<List<Chapter>> { emitter ->
                if (sl == null || sl.source == null || sl.link.isEmpty()) {
                    emitter.onComplete()
                }
                val sourceId = sl.source.id
                val config = SourceManager.CONFIGS[sourceId]
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
                                            LogUtils.e(
                                                TAG,
                                                "parse chapter title is ${chapter.title}"
                                            )
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
                }
            }.subscribeOn(Schedulers.newThread()).observeOn(AndroidSchedulers.mainThread())

        }


        fun content(sl: SearchBook.SL, url: String): Observable<String> {
            return Observable.create<String> {
                if (sl.link.isEmpty() || url.isEmpty()) {
                    it.onError(NullPointerException())
                }
                val sourceId = sl.source.id
                val config = SourceManager.CONFIGS[sourceId]
                if (config.content == null) {
                    it.onError(NullPointerException())
                }
                try {
                    val link = urlVerification(url, sl.link)
                    val jxDocument =
                        JXDocument(Jsoup.connect(link).get())
                    var content = getNodeStr(jxDocument, config.content!!.xpath!!)
                    val builder = java.lang.StringBuilder()
                    val lines = content!!.split(" ".toRegex()).toTypedArray()
                    for (line in lines) {
                        StringUtils.trim(line).trim().let { s ->
                            if (s.isNotEmpty()) {
                                builder.append("     ").append(s).append("\n")
                            }
                        }
                    }
                    val toString = builder.toString()
                    if (toString.replace(" ", "").isNotEmpty()) {
                        content = toString
                    }
                    LogUtils.e("Crawler", "parse content is $content")
                    it.onNext(content)
                } catch (e: java.lang.Exception) {
                    LogUtils.e("Crawler", "crawler book content error is $e")
                    it.onError(e)
                }
            }.subscribeOn(Schedulers.newThread()).observeOn(AndroidSchedulers.mainThread())

        }


        private fun getHomeUrl(): String {
            val checkedMap: SparseBooleanArray = SourceManager.getSourceEnableSparseArray()
            var homeUrl = ""
            for (i in 0.until(checkedMap.size())) {
                val id = SourceManager.CONFIGS.keyAt(i)
                val source = SourceManager.SOURCES.get(id)
                homeUrl = source.homeUrl
            }
            return homeUrl
        }

        fun getRecommend(): Observable<ArrayList<String>> {
            return Observable.create<ArrayList<String>> {
                try {
                    val rs: ArrayList<JXNode?> = ArrayList()
                    val recommend = ArrayList<String>()
                    val checkedMap: SparseBooleanArray = SourceManager.getSourceEnableSparseArray()
                    for (i in 0.until(checkedMap.size())) {
                        val id = SourceManager.CONFIGS.keyAt(i)
                        val config = SourceManager.CONFIGS.valueAt(i)
                        val source = SourceManager.SOURCES.get(id)
                        val jxDocument =
                            JXDocument(Jsoup.connect(source.homeUrl).get())

                        val xpath = config.home!!.recommendPath!!
                        rs.clear()
                        val selN = jxDocument.selN(xpath)
                        rs.addAll(selN)
                        for (value in rs) {
                            val name = getNodeStr(value!!, config.home!!.recommendNamePath!!)
                            name?.let {
                                LogUtils.e(TAG, "BE ADD NAME IS $name")
                                recommend.add(name)
                            }
                        }
                    }
                    it.onNext(recommend)
                } catch (e: java.lang.Exception) {
                    LogUtils.e("Crawler", "crawler get recommend error is $e")
                    it.onError(e)
                }
            }.subscribeOn(Schedulers.newThread()).observeOn(AndroidSchedulers.mainThread())

        }


        fun getHomeJxDocument(): Observable<JXDocument> {
            return Observable.create<JXDocument> {
                try {
                    val checkedMap: SparseBooleanArray = SourceManager.getSourceEnableSparseArray()
                    for (i in 0.until(checkedMap.size())) {
                        val id = SourceManager.CONFIGS.keyAt(i)
                        val source = SourceManager.SOURCES.get(id)
                        val jxDocument =
                            JXDocument(Jsoup.connect(source.homeUrl).get())
                        it.onNext(jxDocument)
                    }
                } catch (e: java.lang.Exception) {
                    LogUtils.e("Crawler", "crawler get recommend error is $e")
                    it.onError(e)
                }
            }.subscribeOn(Schedulers.newThread()).observeOn(AndroidSchedulers.mainThread())

        }

        fun getIndexRecommend(jxDocument: JXDocument): Observable<ArrayList<BookRecommend>> {
            return Observable.create<ArrayList<BookRecommend>> {
                try {
                    val rs: ArrayList<JXNode?> = ArrayList()
                    val recommend = ArrayList<BookRecommend>()
                    val checkedMap: SparseBooleanArray = SourceManager.getSourceEnableSparseArray()
                    for (i in 0.until(checkedMap.size())) {
                        val config = SourceManager.CONFIGS.valueAt(i)
                        val xpath = config.home!!.indexRecommendListPath
                        rs.clear()
                        val selN = jxDocument.selN(xpath)
                        rs.addAll(selN)
                        for (value in rs) {
                            val bookRecommend = BookRecommend()
                            val name = getNodeStr(value!!, config.home!!.indexRecommendNamePath!!)
                            name?.let {
                                LogUtils.e(TAG, "BE ADD NAME IS $it")
                                bookRecommend.bookName = name
                            }
                            val cover = getNodeStr(value!!, config.home!!.indexRecommendCoverPath!!)
                            cover?.let {
                                LogUtils.e(TAG, "BE ADD COVER IS $it")
                                bookRecommend.bookCoverUrl = it
                            }
                            val url = getNodeStr(value!!, config.home!!.indexRecommendUrlPath!!)
                            url?.let {
                                LogUtils.e(TAG, "BE ADD URL IS $it")
                                bookRecommend.bookUrl = it
                            }
                            val descriptor =
                                getNodeStr(value!!, config.home!!.indexRecommendDescriptorPath!!)
                            descriptor?.let {
                                LogUtils.e(TAG, "BE ADD DESCRIPTOR IS $it")
                                bookRecommend.bookDescriptor = it
                            }

                            bookRecommend.bookType = BookRecommend.INDEX_RECOMMEND
                            recommend.add(bookRecommend)
                        }
                    }


                    it.onNext(recommend)
                } catch (e: java.lang.Exception) {
                    LogUtils.e("Crawler", "crawler get recommend error is $e")
                    it.onError(e)
                }
            }.subscribeOn(Schedulers.newThread()).observeOn(AndroidSchedulers.mainThread())
        }


        private fun getXPathList(xpath: String, jxDocument: JXDocument): ArrayList<JXNode?> {
            val rs = ArrayList<JXNode?>()
            rs.clear()
            val selN = jxDocument.selN(xpath)
            rs.addAll(selN)
            return rs
        }


        fun getRecommendByType(type: Int, jxDocument: JXDocument): Observable<ArrayList<String>> {
            return Observable.create<ArrayList<String>> {
                try {
                    val rs: ArrayList<JXNode?> = ArrayList()
                    val recommend = ArrayList<String>()
                    val checkedMap: SparseBooleanArray = SourceManager.getSourceEnableSparseArray()

                    var targetTypeName = ""
                    when (type) {
                        BookRecommend.CITY_RECOMMEND -> {
                            targetTypeName = "都市"
                        }
                        BookRecommend.FANTASY_RECOMMEND -> {
                            targetTypeName = "玄幻"
                        }
                        BookRecommend.GAME_RECOMMEND -> {
                            targetTypeName = "网游"
                        }
                        BookRecommend.COMPREHENSION_RECOMMEND -> {
                            targetTypeName = "修真"
                        }
                        BookRecommend.HISTORY_RECOMMEND -> {
                            targetTypeName = "历史"
                        }
                        BookRecommend.SCIENCE_RECOMMEND -> {
                            targetTypeName = "科幻"
                        }

                    }

                    for (i in 0.until(checkedMap.size())) {
                        var finalXPath = ""
                        val config = SourceManager.CONFIGS.valueAt(i)
                        val xpath = config.home!!.contentNovelRecommendListPath


                        val xPathList = getXPathList(xpath!!, jxDocument)
                        var pass = false
                        for (value in xPathList) {
                            val nodeStr = getNodeStr(value!!, config.home!!.contentTypeNamePath!!)
                            nodeStr?.let { typeName ->
                                if (typeName.contains(targetTypeName)) {
                                    pass = true
                                }
                            }
                            if (pass) {
                                break
                            }
                        }

                        if (!pass) {
                            val xpath = config.home!!.contentBordNovelRecommendListPath
                            val xPathList = getXPathList(xpath!!, jxDocument)
                            for (value in xPathList) {
                                val nodeStr =
                                    getNodeStr(value!!, config.home!!.contentTypeNamePath!!)
                                nodeStr?.let { typeName ->
                                    if (typeName.contains(targetTypeName)) {
                                        pass = true
                                    }
                                }
                                if (pass) {
                                    break
                                }
                            }
                            if (pass) {
                                finalXPath = config.home!!.contentBordNovelRecommendListPath!!
                            }
                        } else {
                            finalXPath = config.home!!.contentNovelRecommendListPath!!
                        }

                        if (finalXPath.isNotEmpty()) {
                            rs.clear()
                            val selN = jxDocument.selN(finalXPath)
                            rs.addAll(selN)
                        } else {
                            it.onError(NullPointerException())
                        }
                        for (value in rs) {
                            value?.let {
                                val typeName =
                                    getNodeStr(value, config.home!!.contentTypeNamePath!!)
                                typeName?.let {
                                    if (typeName.contains(targetTypeName)) {
                                        val nodeStr =
                                            getNodeStr(value, config.home!!.contentTopUrl!!)
                                        nodeStr?.let { topUrl ->
                                            var url = topUrl
                                            if (!topUrl.contains("http")) {
                                                url = getHomeUrl() + topUrl
                                            }
                                            recommend.add(url)
                                        }
                                        val sel = value.sel(config.home!!.contentListPath!!)
                                        sel?.let {
                                            for (content in sel) {
                                                content?.let {
                                                    val nodeStr = getNodeStr(
                                                        content,
                                                        config.home!!.contentItemUrlPath!!
                                                    )
                                                    nodeStr?.let {
                                                        var url = nodeStr
                                                        if (!nodeStr.contains("http")) {
                                                            url = getHomeUrl() + nodeStr
                                                        }
                                                        recommend.add(url!!)
                                                    }
                                                }

                                            }
                                        }
                                    }
                                }
                            }
                        }
                        it.onNext(recommend)
                    }


                } catch (e: java.lang.Exception) {
                    LogUtils.e("Crawler", "crawler get recommend error is $e")
                    it.onError(e)
                }
            }.subscribeOn(Schedulers.newThread()).observeOn(AndroidSchedulers.mainThread())

        }


        private fun nameToType(name: String): Int {
            return when (name) {
                "玄幻小说" -> {
                    BookRecommend.FANTASY_RECOMMEND
                }
                "修真小说" -> {
                    BookRecommend.COMPREHENSION_RECOMMEND
                }
                "历史小说" -> {
                    BookRecommend.HISTORY_RECOMMEND
                }
                "网游小说" -> {
                    BookRecommend.GAME_RECOMMEND
                }
                "科幻小说" -> {
                    BookRecommend.SCIENCE_RECOMMEND
                }
                "言情小说" -> {
                    BookRecommend.ROMANS_RECOMMEND
                }
                "全本小说" -> {
                    BookRecommend.ALLBOOK_RECOMMEND
                }
                "其他小说" -> {
                    BookRecommend.ORTHER_RECOMMEND
                }
                "都市小说" -> {
                    BookRecommend.CITY_RECOMMEND
                }
                else -> {
                    -1
                }
            }
        }


        fun getTypeUrls(jxDocument: JXDocument): Observable<HashMap<Int, String>> {
            return Observable.create<HashMap<Int, String>> {
                try {
                    val rs: ArrayList<JXNode?> = ArrayList()
                    val checkedMap: SparseBooleanArray = SourceManager.getSourceEnableSparseArray()
                    val hashMap = HashMap<Int, String>()
                    for (i in 0.until(checkedMap.size())) {
                        hashMap.clear()
                        val config = SourceManager.CONFIGS.valueAt(i)
                        val xpath = config.home!!.concurrentTypeUrlsPath
                        rs.clear()
                        val selN = jxDocument.selN(xpath)
                        rs.addAll(selN)
                        for (value in rs) {

                            val name = getNodeStr(value!!, config.home!!.concurrentTypeNamePath!!)
                            name?.let {
                                val nameToType = nameToType(name)
                                val nodeStr = getNodeStr(
                                    value!!,
                                    config.home!!.concurrentTypeUrlPath!!
                                )
                                nodeStr?.let { url -> hashMap.put(nameToType, getHomeUrl() + url)
                                }
                            }
                        }
                    }
                    it.onNext(hashMap)
                } catch (e: java.lang.Exception) {
                    LogUtils.e("Crawler", "crawler get recommend error is $e")
                    it.onError(e)
                }
            }.subscribeOn(Schedulers.newThread()).observeOn(AndroidSchedulers.mainThread())
        }


        fun getConcurrentTypeRankingList(type:Int,url: String): Observable<List<BookRecommend>> {
            return Observable.create<List<BookRecommend>> {
                val checkedMap: SparseBooleanArray = SourceManager.getSourceEnableSparseArray()
                val recommends = ArrayList<BookRecommend>()
                val rs: ArrayList<JXNode?> = ArrayList()
                for (i in 0.until(checkedMap.size())) {
                    recommends.clear()
                    val config = SourceManager.CONFIGS.valueAt(i)
                    val jxDocument =
                        JXDocument(Jsoup.connect(url).get())

                    config?.let {

                        var listPath = config.type!!.typeRankingPath
                        if(type == BookRecommend.ALLBOOK_RECOMMEND){
                            listPath = config.type!!.allTypeRankingPath
                        }
                        rs.clear()
                        val sel = jxDocument.selN(listPath)
                        rs.addAll(sel)
                        for (node in rs) {
                            node?.let {
                                try {
                                    val bookRecommend = BookRecommend()
                                    val bookUrl =
                                        getNodeStr(node, config.type!!.typeRankingUrlPath!!)!!
                                    val name =
                                        getNodeStr(node, config.type!!.typeRankingNamePath!!)!!
                                    LogUtils.e("Crawler", "name is $name  url is $bookUrl")

                                    if(bookUrl.isNotEmpty()){
                                        bookRecommend.bookName = name
                                        bookRecommend.bookUrl = getHomeUrl() + bookUrl
                                        bookRecommend.bookType = type
                                        recommends.add(bookRecommend)
                                    }

                                } catch (e: java.lang.Exception) {
                                    LogUtils.e("Crawler", "load concurrent type error is $e")
                                }
                            }
                        }
                    }
                }
                it.onNext(recommends)

            }
        }


        fun getBookDetail(url: String): Observable<BookRecommend> {
            return Observable.create<BookRecommend> {

                try {
                    val bookRecommend = BookRecommend()
                    bookRecommend.bookUrl = url
                    val checkedMap: SparseBooleanArray = SourceManager.getSourceEnableSparseArray()
                    for (i in 0.until(checkedMap.size())) {
                        val config = SourceManager.CONFIGS.valueAt(i)
                        LogUtils.e("Crawler", "getBookDetail connect to url $url")
                        val jxDocument =
                            JXDocument(Jsoup.connect(url).get())


                        config?.let { sourceConfig ->
                            sourceConfig.content?.let { content ->
                                val cover = getNodeStr(jxDocument, content.cover!!)
                                val bookName = getNodeStr(jxDocument, content.bookName!!)
                                val descriptor = getNodeStr(jxDocument, content.descriptor!!)
                                val selN = jxDocument.selN(content.infoList)


                                cover?.let {
                                    bookRecommend.bookCoverUrl = cover
                                }

                                bookName?.let {
                                    bookRecommend.bookName = bookName
                                }


                                descriptor?.let {
                                    bookRecommend.bookDescriptor = descriptor
                                }



                                LogUtils.e("Crawler", "cover is $cover")
                                LogUtils.e("Crawler", "bookName is $bookName")
                                LogUtils.e("Crawler", "descriptor is $descriptor")


                                try {
                                    val author = selN.first()
                                    author?.let {
                                        val authorName = getNodeStr(author, "/text()")
                                        authorName?.let {
                                            bookRecommend.authorName = authorName
                                        }
                                        LogUtils.e("Crawler", "author name is $authorName")
                                    }
                                } catch (e: Exception) {
                                    LogUtils.e("Crawler", "get author name error is $e")
                                }




                                try {
                                    val updateTime = selN[2]
                                    updateTime?.let {
                                        val s = getNodeStr(updateTime, "/text()")
                                        LogUtils.e("Crawler", "update time is $s")
                                    }
                                } catch (e: Exception) {
                                    LogUtils.e("Crawler", "get update time error is $e")
                                }




                                try {
                                    val newChapter = selN[3]
                                    newChapter?.let {
                                        val s = getNodeStr(newChapter, "/text()")
                                        LogUtils.e("Crawler", "new chapter is $s")
                                        val chapterUrl =
                                            getNodeStr(newChapter, content!!.newChapterUrl!!)
                                        LogUtils.e("Crawler", "new chapter url is $chapterUrl")
                                    }
                                } catch (e: Exception) {
                                    LogUtils.e("Crawler", "get new chapter error is $e")
                                }


                            }
                        }
                        it.onNext(bookRecommend)
                    }
                } catch (e: java.lang.Exception) {
                    LogUtils.e("Crawler", "get book detail error is $e")
                }

            }
        }


        fun getConcurrentTypeRecommend(
            url: String,
            type: Int
        ): Observable<ArrayList<BookRecommend>> {
            return Observable.create<ArrayList<BookRecommend>> {
                try {
                    val rs: ArrayList<JXNode?> = ArrayList()
                    val recommend = ArrayList<BookRecommend>()
                    val checkedMap: SparseBooleanArray = SourceManager.getSourceEnableSparseArray()
                    for (i in 0.until(checkedMap.size())) {
                        val config = SourceManager.CONFIGS.valueAt(i)
                        val jxDocument =
                            JXDocument(Jsoup.connect(url).get())

                        val xpath = config.type!!.recommendListPath
                        rs.clear()
                        val selN = jxDocument.selN(xpath)
                        rs.addAll(selN)
                        for (value in rs) {
                            val bookRecommend = BookRecommend()
                            val name = getNodeStr(value!!, config.type!!.recommendNamePath!!)
                            name?.let {
                                LogUtils.e(TAG, "BE ADD NAME IS $name  URL IS $url")
                                bookRecommend.bookName = name
                            }

                            val urlPath = getNodeStr(value!!, config.type!!.recommendUrlPath!!)
                            urlPath?.let {
                                LogUtils.e(TAG, "BE ADD URL IS $urlPath URL IS $url")
                                bookRecommend.bookUrl = urlPath
                            }

                            val cover = getNodeStr(value!!, config.type!!.recommendCoverPath!!)
                            cover?.let {
                                LogUtils.e(TAG, "BE ADD COVER IS $cover URL IS $url")
                                bookRecommend.bookCoverUrl = cover
                            }

                            bookRecommend.bookType = type
                            recommend.add(bookRecommend)
                        }
                    }


                    it.onNext(recommend)
                } catch (e: java.lang.Exception) {
                    LogUtils.e("Crawler", "crawler get recommend error is $e")
                    it.onError(e)
                }
            }.subscribeOn(Schedulers.newThread()).observeOn(AndroidSchedulers.mainThread())

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