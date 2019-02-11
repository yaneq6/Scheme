package io.fluks.android

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.databinding.ViewDataBinding
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.view.View
import android.widget.ProgressBar
import io.fluks.android.databinding.DataBindingDelegate
import io.fluks.common.*
import timber.log.Timber
import java.lang.ref.WeakReference

abstract class BaseActivity<DataBinding, Component> :

    AppCompatActivity(),
    DataBindingDelegate<DataBinding>,
    Depends<Component>,
    DispatchDelegate<Event>,
    UI.Finishable

    where DataBinding : ViewDataBinding,
          Component : Dispatch.Component,
          Component : UI.Component<DataBinding>,
          Component : BaseActivity.Component {

    final override val containerView: View? get() = binding.root
    override val dispatch: Dispatch<Event> get() = component.dispatch

    val binding by lazyDi { setBindingView(layoutId) }

    val toolbar: Toolbar? get() = findViewById(R.id.toolbar)
    val progress: ProgressBar? get() = findViewById(R.id.progress)

    final override fun onCreate(savedInstanceState: Bundle?): Unit = this.measure("onCreate") {
        Timber.d("onCreate started")
        dispatch(Platform::OnTop)
        super.onCreate(savedInstanceState)
        binding
        setSupportActionBar(toolbar)
        di {
            binding.bind()
            intent.event?.let { dispatch(it) }
            initDebugDrawer()
        }
        onCreateSafe(savedInstanceState)
    }

    protected open fun onCreateSafe(savedInstanceState: Bundle?) {/*no-op*/
    }

    override fun onResume() {
        dispatch(Platform::OnTop)
        super.onResume()
    }

    override fun onNewIntent(intent: Intent): Unit = dispatch(intent.event)

    override fun onDestroy() {
        binding.unbind()
        component.disposable.dispose()
        super.onDestroy()
    }

    fun dispatch(getEffect: (WeakReference<Context>) -> Effect) = dispatch(
        getEffect(this@BaseActivity.weak())
    )


    interface Component {
        fun Activity.initDebugDrawer()
    }
}