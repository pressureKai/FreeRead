package com.kai.ui.rankingdetail

import com.alibaba.android.arouter.facade.annotation.Route
import com.kai.base.R
import com.kai.base.activity.BaseMvpActivity
import kotlinx.android.synthetic.main.merge_toolbar.*

@Route(path = "/app/ranking")
class RankingDetailActivity: BaseMvpActivity<RankingDetailContract.View, RankingDetailPresenter>(),
    RankingDetailContract.View {
    override fun initView() {
        initImmersionBar(view = toolbar,fitSystem = false)

    }

    override fun setLayoutId(): Int {
        return R.layout.activity_ranking_detail
    }
}