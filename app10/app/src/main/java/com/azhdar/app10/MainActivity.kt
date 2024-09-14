package com.azhdar.app10

import android.app.Activity

import android.content.Context
import android.content.res.Configuration
import android.os.Bundle
import android.view.LayoutInflater

import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.core.content.ContextCompat.startActivity
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.azhdar.app10.ui.theme.App10Theme
import com.github.dhaval2404.colorpicker.ColorPickerDialog
import com.github.dhaval2404.colorpicker.MaterialColorPickerDialog
import com.github.dhaval2404.colorpicker.model.ColorShape
import com.github.dhaval2404.colorpicker.model.ColorSwatch
import com.github.skydoves.colorpicker.compose.ColorEnvelope
import com.github.skydoves.colorpicker.compose.HsvColorPicker
import com.github.skydoves.colorpicker.compose.rememberColorPickerController
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import ir.tapsell.plus.AdRequestCallback
import ir.tapsell.plus.AdShowListener
import ir.tapsell.plus.TapsellPlus
import ir.tapsell.plus.TapsellPlusBannerType
import ir.tapsell.plus.TapsellPlusInitListener
import ir.tapsell.plus.model.AdNetworkError
import ir.tapsell.plus.model.AdNetworks
import ir.tapsell.plus.model.TapsellPlusAdModel
import ir.tapsell.plus.model.TapsellPlusErrorModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : ComponentActivity() {


    private var responseId: String? = null
    private var responseId3: String? = null
    private var rewardedResponseId:String?=null
    var myColor= Color.Black
    var mainbackgroundColor=Color.Black
    var MainActivityContext=this

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            App10Theme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
//                    color = MaterialTheme.colorScheme.background
                )
                {
                   ui3()
                }
            }
        }
        init_tapsell()

    }

    fun init_tapsell(){
        TapsellPlus.initialize(this,
            "your related tapsell plus key",
            object : TapsellPlusInitListener {
                override fun onInitializeSuccess(adNetworks: AdNetworks) {

                }

                override fun onInitializeFailed(
                    adNetworks: AdNetworks,
                    adNetworkError: AdNetworkError
                ) {

                }
            })

    }




    @Composable
    fun ui3(){









        var context2= LocalContext.current as Activity
        var window=context2.window
        WindowCompat.setDecorFitsSystemWindows(window, true)
        val windowInsetsController = WindowCompat.getInsetsController(window, window.decorView)
        windowInsetsController?.let {
            it.show(WindowInsetsCompat.Type.systemBars())


        }



        var systemUiController= rememberSystemUiController()
        systemUiController.setStatusBarColor(mainbackgroundColor)
        systemUiController.setNavigationBarColor(mainbackgroundColor)




        val navController = rememberNavController()
        NavHost(navController = navController, startDestination = "ui"
            , enterTransition = {

                this.slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Down)
            },
            exitTransition = {
//                ExitTransition.None
                this.slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Up)
            }
        ){
            composable("ui"
            ){
                ui(navController = navController)
            }
            composable("fragment"){
                MyFragment( show = true, navController = navController)
            }
            composable("greeting"){
                Greeting(name = "ssssss",navController = navController)
            }
            composable("alireza"){
                alireza(navController)
            }
            composable("ali"){
                ali(navController)
            }




        }


    }



    @Composable
    fun ui(navController: NavHostController){
        val context= LocalContext.current
        val configuration= LocalConfiguration.current
        var width=configuration.screenWidthDp
        var hight=configuration.screenHeightDp


        var viewModel1:myViewModel= viewModel()







        var a=Flashlight()

        //request banner ads here to handle recomposition

        var isDarkMode= isSystemInDarkTheme()
        var flag by remember{
            mutableStateOf(isDarkMode)
        }

        mainbackgroundColor=
            if(flag)
                Color(0xFF3F51B5)
            else
                Color(0xFFE67B7B)


        var context2= LocalContext.current as Activity
        var window=context2.window
        WindowCompat.setDecorFitsSystemWindows(window, true)
        val windowInsetsController = WindowCompat.getInsetsController(window, window.decorView)
        windowInsetsController?.let {
            it.show(WindowInsetsCompat.Type.systemBars())


        }
        var systemUiController= rememberSystemUiController()
        systemUiController.setStatusBarColor(mainbackgroundColor)
        systemUiController.setNavigationBarColor(mainbackgroundColor)



        requestAd()

        requestAd3()






        if(configuration.orientation== Configuration.ORIENTATION_PORTRAIT){




ConstraintLayout(
    Modifier.fillMaxSize()
        .background(mainbackgroundColor)
) {
    var (box,ad) = createRefs()

    AndroidView(
        factory = { context: Context ->
        LayoutInflater.from(context).inflate(R.layout.a, null)
    },
        Modifier
        .fillMaxWidth(1f)
        .constrainAs(ad) {
            top.linkTo(parent.top)
            start.linkTo(parent.start)
        }
    )

    Box(
        Modifier.constrainAs(box) {
        centerHorizontallyTo(parent)
        centerVerticallyTo(parent)
    }
    ) {
        ConstraintLayout{

            val ( button1, button2, button3) = createRefs()



//


            var flashlightImage by remember {
                mutableStateOf(

                    if (viewModel1.flashlightState.value)
                        R.drawable.flashlight_on_24px__2_
                    else
                        R.drawable.flashlight_off_24px__2_

                )
            }


            var modifier1 = Modifier
                .constrainAs(button1) {
                    top.linkTo(parent.top)
                    centerHorizontallyTo(parent)
                }
                .clickable {
                    if (viewModel1.flashlightState.value) {
                        viewModel1.toggleFlashlight()
                        a.toggleFlash1(viewModel1.flashlightState.value, context)
                        flashlightImage = R.drawable.flashlight_off_24px__2_

                    } else {
                        viewModel1.toggleFlashlight()
                        a.toggleFlash1(viewModel1.flashlightState.value, context)
                        flashlightImage = R.drawable.flashlight_on_24px__2_
                    }


                }
//                .padding(top = (hight / 10).dp)
//                .padding(bottom = (hight / 20).dp)

                .size((width / 4).dp)

            makeImagebutton(flashlightImage, modifier1)

//                var flashImage by remember {
//                    mutableStateOf(
//                        if(viewModel2.state.value)
//                            R.drawable.flash_on_24px__1_
//                        else
//                            R.drawable.flash_off_24px__1_
//
//
//                    )
//                }

//                getIm(viewModel2.state.value)

            var flashImage by remember {
                mutableStateOf(

                    if (viewModel1.flasherState.value)
                        R.drawable.flash_on_24px__1_
                    else
                        R.drawable.flash_off_24px__1_
                )
            }

            var modifier2 = Modifier
                .constrainAs(button2) {
                    top.linkTo(button1.bottom)
                    centerHorizontallyTo(parent)
                }
                .clickable {


                    if (viewModel1.flasherState.value) {

                        flashImage = R.drawable.flash_off_24px__1_
                        viewModel1.toggleFlasher()
                        a.toggleFlasher1(viewModel1, context)
                    } else {
                        flashImage = R.drawable.flash_on_24px__1_
                        viewModel1.toggleFlasher()
                        a.toggleFlasher1(viewModel1, context)
                    }


                }
//                .padding(bottom = (hight / 30).dp)
                .padding(all = (hight/15).dp)
                .size((width / 4).dp)
            makeImagebutton(flashImage, modifier = modifier2)


            var modifier3 = Modifier
                .constrainAs(button3) {
                    top.linkTo(button2.bottom)
                    centerHorizontallyTo(parent)
                }
                .clickable {
                    navController.navigate("fragment")
                    showAd3()
                }

                .size((width / 5).dp)
            makeImagebutton(R.drawable.wb_sunny_24px, modifier = modifier3)


        }
    }
}

        }
        else if(configuration.orientation== Configuration.ORIENTATION_LANDSCAPE){

            ConstraintLayout(Modifier.fillMaxSize()){

                val horizontalGuideline=createGuidelineFromStart(0.27f)
                val (ad,button1, button2, button3) = createRefs()

//            createVerticalChain(
//                button1
//                ,button2
//                ,button3
//                , chainStyle = ChainStyle.Packed
//            )



                requestAd()
                requestAd3()


                AndroidView(factory = {context: Context ->
                    LayoutInflater.from(context).inflate(R.layout.a,null)
                }, Modifier
                    .fillMaxWidth(1f)
                    .constrainAs(ad) {
                        top.linkTo(parent.top)
                        start.linkTo(parent.start)
                    })


                var flashlightImage by remember {
                    mutableStateOf(
                        if(viewModel1.flashlightState.value)
                            R.drawable.flashlight_on_24px__2_
                        else
                            R.drawable.flashlight_off_24px__2_




                    )
                }
                var modifier1= Modifier
                    .constrainAs(button1) {
                        start.linkTo(horizontalGuideline)
                        centerVerticallyTo(parent)
                    }
                    .clickable {
                        if (viewModel1.flashlightState.value) {
                            flashlightImage = R.drawable.flashlight_off_24px__2_
                            viewModel1.toggleFlashlight()
                        } else {
                            flashlightImage = R.drawable.flashlight_on_24px__2_
                            viewModel1.toggleFlashlight()
                        }
                    }
                    .padding(bottom = (width / 20).dp)
                    .size((hight / 3).dp)
                makeImagebutton( flashlightImage, modifier =modifier1 )

                var flashImage by remember {
                    mutableStateOf(
                        if(viewModel1.flasherState.value)
                            R.drawable.flash_on_24px__1_
                        else
                            R.drawable.flash_off_24px__1_




                    )
                }
                var modifier2= Modifier
                    .constrainAs(button2) {
                        start.linkTo(button1.end)
                        centerVerticallyTo(parent)
                    }
                    .clickable {
                        if (viewModel1.flasherState.value) {
                            flashImage = R.drawable.flash_off_24px__1_
                            viewModel1.toggleFlasher()
                        } else {
                            flashImage = R.drawable.flash_on_24px__1_
                            viewModel1.toggleFlasher()
                        }
                    }
                    .padding(bottom = (width / 30).dp)
                    .size((hight / 3).dp)
                makeImagebutton( flashImage, modifier = modifier2)

                var modifier3= Modifier
                    .constrainAs(button3) {
                        start.linkTo(button2.end)
                        centerVerticallyTo(parent)
                    }
                    .clickable {
                        navController.navigate("fragment")

                        showAd3()

                    }
                    .size((hight / 3).dp)
                    .padding(bottom = (width / 30).dp)
                makeImagebutton( R.drawable.wb_sunny_24px, modifier =modifier3 )
            }







        }









    }



    public fun requestAd3(){

        TapsellPlus.requestInterstitialAd(
            this, "your related tapsell plus key",
            object : AdRequestCallback() {
                override fun response(tapsellPlusAdModel: TapsellPlusAdModel) {
                    super.response(tapsellPlusAdModel)
                    if (isDestroyed) return
                    responseId3 = tapsellPlusAdModel.responseId


                }

                override fun error(message: String) {
                    if (isDestroyed) return

                }
            })

    }
    public fun showAd3(){


        TapsellPlus.showInterstitialAd(this, responseId3,
            object : AdShowListener() {
                override fun onOpened(tapsellPlusAdModel: TapsellPlusAdModel) {
                    super.onOpened(tapsellPlusAdModel)

                }

                override fun onClosed(tapsellPlusAdModel: TapsellPlusAdModel) {
                    super.onClosed(tapsellPlusAdModel)

                }

                override fun onError(tapsellPlusErrorModel: TapsellPlusErrorModel) {
                    super.onError(tapsellPlusErrorModel)

                }
            })

    }

    public fun requestAd2(){

            TapsellPlus.requestRewardedVideoAd(
                this,
                "your related tapsell plus key",object :AdRequestCallback() {
                   override fun response( tapsellPlusAdModel:TapsellPlusAdModel) {
                        super.response(tapsellPlusAdModel);

                       if (isDestroyed) return

                        //Ad is ready to show
                        //Put the ad's responseId to your responseId variable
                        rewardedResponseId = tapsellPlusAdModel.getResponseId();

                    }

                    override fun  error( message:String) {
                    }

                }

                );

    }
     public fun showAd2(){
        TapsellPlus.showRewardedVideoAd(
            this
            ,rewardedResponseId,
            object : AdShowListener() {
                override fun onOpened(tapsellPlusAdModel: TapsellPlusAdModel) {
                    super.onOpened(tapsellPlusAdModel)
                }

                override fun onClosed(tapsellPlusAdModel: TapsellPlusAdModel) {
                    super.onClosed(tapsellPlusAdModel)
                }


                override fun onError(tapsellPlusErrorModel: TapsellPlusErrorModel) {
                    super.onError(tapsellPlusErrorModel)
                }
            })
    }









    private fun requestAd() {
        TapsellPlus.requestStandardBannerAd(
            this, "your related tapsell plus key",
            TapsellPlusBannerType.BANNER_320x50,
            object : AdRequestCallback() {
                override fun response(tapsellPlusAdModel: TapsellPlusAdModel) {
                    super.response(tapsellPlusAdModel)
                    if (isDestroyed) return

                    responseId = tapsellPlusAdModel.responseId

                    showAd()

                }

                override fun error(message: String) {
                    if (isDestroyed) return

                }
            })
    }

    private fun showAd() {
        TapsellPlus.showStandardBannerAd(this, responseId,
            findViewById(R.id.standardBanner),
            object : AdShowListener() {
                override fun onOpened(tapsellPlusAdModel: TapsellPlusAdModel) {
                    super.onOpened(tapsellPlusAdModel)

                }

                override fun onError(tapsellPlusErrorModel: TapsellPlusErrorModel) {
                    super.onError(tapsellPlusErrorModel)

                }
            })

    }

    private fun destroyAd() {
        TapsellPlus.destroyStandardBanner(this, responseId, findViewById(R.id.standardBanner))
    }


    @Composable
    fun makeImagebutton(image:Int, modifier: Modifier){
        Image(painter = painterResource(id = image)
            , contentDescription ="abc"
            ,modifier
        )
    }





    @Composable
    fun MyFragment(show:Boolean,navController: NavHostController) {

        var context= LocalContext.current

        var isDarkMode= isSystemInDarkTheme()
        var flag by remember{
            mutableStateOf(isDarkMode)
        }

        mainbackgroundColor=
            if(flag)
                Color(0xFF3F51B5)
            else
                Color(0xFFE67B7B)



        var context2= LocalContext.current as Activity
        var window=context2.window
        WindowCompat.setDecorFitsSystemWindows(window, true)
        val windowInsetsController = WindowCompat.getInsetsController(window, window.decorView)
        windowInsetsController?.let {
            it.show(WindowInsetsCompat.Type.systemBars())


        }
        var systemUiController= rememberSystemUiController()
        systemUiController.setStatusBarColor(mainbackgroundColor)
        systemUiController.setNavigationBarColor(mainbackgroundColor)






        ConstraintLayout(
            Modifier
                .fillMaxSize()
                .background(mainbackgroundColor)) {

            val q=createRef()

            Box(modifier = Modifier.constrainAs(q){
                centerVerticallyTo(parent)
                centerHorizontallyTo(parent)
            }) {
                ConstraintLayout() {
                    val (a, b, c, d, e) = createRefs()
                    IconButton(onClick = {
                        myColor=Color.White
                        navController.navigate("alireza")
                                         /*TODO*/ },
                        Modifier
                            .constrainAs(a) {
                                centerHorizontallyTo(parent)
                                top.linkTo(parent.top)
                            }
                            .padding(20.dp)
                            .border(2.dp, Color.Cyan, shape = CircleShape)
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.white),
                            contentDescription = "a"
                        )
                    }
                    IconButton(onClick = {
                        myColor=Color.Red
                        navController.navigate("alireza")
                                         /*TODO*/ },
                        Modifier
                            .constrainAs(b) {
                                centerHorizontallyTo(parent)
                                top.linkTo(a.bottom)
                            }
                            .padding(20.dp)
                            .border(2.dp, Color.Cyan, shape = CircleShape)
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.red),
                            contentDescription = "a"
                        )
                    }
                    IconButton(
                        onClick = { /*TODO*/
                            myColor=Color.Green
                            navController.navigate("alireza")


                                  },
                        Modifier
                            .constrainAs(c) {
                                centerHorizontallyTo(parent)
                                top.linkTo(b.bottom)
                            }
                            .padding(20.dp)
                            .border(2.dp, Color.Cyan, shape = CircleShape)
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.green),
                            contentDescription = "a"
                        )
                    }
                    IconButton(
                        onClick = { /*TODO*/

                            myColor=Color.Blue
                            navController.navigate("alireza")




                                  },
                        Modifier
                            .constrainAs(d) {
                                centerHorizontallyTo(parent)
                                top.linkTo(c.bottom)
                            }
                            .padding(20.dp)
                            .border(2.dp, Color.Cyan, shape = CircleShape)
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.blue),
                            contentDescription = "a"
                        )
                    }

                    IconButton(onClick = {

                        navController.navigate("greeting")




                    },
                        Modifier
                            .constrainAs(e) {
                                centerHorizontallyTo(parent)
                                top.linkTo(d.bottom)
                            }
                            .padding(20.dp)
                            .border(2.dp, Color.Cyan, shape = CircleShape)

                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.colorpicker),
                            contentDescription = "a"
                        )
                    }
                }
            }
        }
    }


    override fun onDestroy() {
        super.onDestroy()

    }



    @Composable
    fun Greeting(name: String, modifier: Modifier = Modifier,navController: NavHostController) {

        var context1= LocalContext.current as Activity
        var window=context1.window
        WindowCompat.setDecorFitsSystemWindows(window, true)
        val windowInsetsController = WindowCompat.getInsetsController(window, window.decorView)
        windowInsetsController?.let {
            it.show(WindowInsetsCompat.Type.systemBars())
            it.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }

        val controller = rememberColorPickerController()
        var context=LocalContext.current
      var myColor by remember {
            mutableStateOf(Color.Black)
        }

        ConstraintLayout(
            Modifier
                .fillMaxSize()
                .background(myColor)
        ){
            var (colorpicker,button)=createRefs()
            HsvColorPicker(
                modifier = Modifier
                    .constrainAs(colorpicker) {
                        centerHorizontallyTo(parent)
                        centerVerticallyTo(parent)
                    }
                    .background(myColor)
                    .width(200.dp)
                    .height(200.dp)
                    .padding(10.dp),
                controller = controller,
                onColorChanged = { colorEnvelope: ColorEnvelope ->
                    // do something

                    myColor=colorEnvelope.color
                }
            )
            Button(
                onClick = { /*TODO*/
                    MainActivityContext.myColor=myColor
                    navController.navigate("alireza")

                },
                Modifier
                    .constrainAs(button){
                        top.linkTo(colorpicker.bottom)
                        centerHorizontallyTo(parent)
                    }
            ) {
                Text(text = "select")
            }
        }
    }
    @Composable
    fun alireza(navController: NavHostController){
        var context= LocalContext.current as Activity
        var window=context.window
        WindowCompat.setDecorFitsSystemWindows(window, false)
        val windowInsetsController = WindowCompat.getInsetsController(window, window.decorView)
        windowInsetsController?.let {
            it.hide(WindowInsetsCompat.Type.systemBars())
            it.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE

        }
        Box(modifier = Modifier
            .fillMaxSize()
            .background(myColor)){

        }

//
//        var systemUiController= rememberSystemUiController()
//        systemUiController.setStatusBarColor(myColor)

//

    }


    @Composable
   fun ali(navController: NavHostController){
        var isDarkMode= isSystemInDarkTheme()
        var flag by remember{
            mutableStateOf(isDarkMode)
        }

        mainbackgroundColor=
            if(flag)
                Color(0xFF3F51B5)
            else
                Color(0xFFE67B7B)

        var context2= LocalContext.current as Activity
        var window=context2.window
        WindowCompat.setDecorFitsSystemWindows(window, true)
        val windowInsetsController = WindowCompat.getInsetsController(window, window.decorView)
        windowInsetsController?.let {
            it.show(WindowInsetsCompat.Type.systemBars())
            it.hide(WindowInsetsCompat.Type.navigationBars())

        }
        var systemUiController= rememberSystemUiController()
        systemUiController.setStatusBarColor(mainbackgroundColor)
        systemUiController.setNavigationBarColor(mainbackgroundColor)


        Box(
            Modifier
                .fillMaxSize()
                .background(mainbackgroundColor)){

        }


       LaunchedEffect(key1 = true){
           delay(100)
           navController.navigate("ui")
       }



    }

}

