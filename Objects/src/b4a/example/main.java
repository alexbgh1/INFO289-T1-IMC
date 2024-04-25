package b4a.example;


import anywheresoftware.b4a.B4AMenuItem;
import android.app.Activity;
import android.os.Bundle;
import anywheresoftware.b4a.BA;
import anywheresoftware.b4a.BALayout;
import anywheresoftware.b4a.B4AActivity;
import anywheresoftware.b4a.ObjectWrapper;
import anywheresoftware.b4a.objects.ActivityWrapper;
import java.lang.reflect.InvocationTargetException;
import anywheresoftware.b4a.B4AUncaughtException;
import anywheresoftware.b4a.debug.*;
import java.lang.ref.WeakReference;

public class main extends Activity implements B4AActivity{
	public static main mostCurrent;
	static boolean afterFirstLayout;
	static boolean isFirst = true;
    private static boolean processGlobalsRun = false;
	BALayout layout;
	public static BA processBA;
	BA activityBA;
    ActivityWrapper _activity;
    java.util.ArrayList<B4AMenuItem> menuItems;
	public static final boolean fullScreen = true;
	public static final boolean includeTitle = false;
    public static WeakReference<Activity> previousOne;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        mostCurrent = this;
		if (processBA == null) {
			processBA = new BA(this.getApplicationContext(), null, null, "b4a.example", "b4a.example.main");
			processBA.loadHtSubs(this.getClass());
	        float deviceScale = getApplicationContext().getResources().getDisplayMetrics().density;
	        BALayout.setDeviceScale(deviceScale);
            
		}
		else if (previousOne != null) {
			Activity p = previousOne.get();
			if (p != null && p != this) {
                BA.LogInfo("Killing previous instance (main).");
				p.finish();
			}
		}
        processBA.setActivityPaused(true);
        processBA.runHook("oncreate", this, null);
		if (!includeTitle) {
        	this.getWindow().requestFeature(android.view.Window.FEATURE_NO_TITLE);
        }
        if (fullScreen) {
        	getWindow().setFlags(android.view.WindowManager.LayoutParams.FLAG_FULLSCREEN,   
        			android.view.WindowManager.LayoutParams.FLAG_FULLSCREEN);
        }
		
        processBA.sharedProcessBA.activityBA = null;
		layout = new BALayout(this);
		setContentView(layout);
		afterFirstLayout = false;
        WaitForLayout wl = new WaitForLayout();
        if (anywheresoftware.b4a.objects.ServiceHelper.StarterHelper.startFromActivity(processBA, wl, false))
		    BA.handler.postDelayed(wl, 5);

	}
	static class WaitForLayout implements Runnable {
		public void run() {
			if (afterFirstLayout)
				return;
			if (mostCurrent == null)
				return;
            
			if (mostCurrent.layout.getWidth() == 0) {
				BA.handler.postDelayed(this, 5);
				return;
			}
			mostCurrent.layout.getLayoutParams().height = mostCurrent.layout.getHeight();
			mostCurrent.layout.getLayoutParams().width = mostCurrent.layout.getWidth();
			afterFirstLayout = true;
			mostCurrent.afterFirstLayout();
		}
	}
	private void afterFirstLayout() {
        if (this != mostCurrent)
			return;
		activityBA = new BA(this, layout, processBA, "b4a.example", "b4a.example.main");
        
        processBA.sharedProcessBA.activityBA = new java.lang.ref.WeakReference<BA>(activityBA);
        anywheresoftware.b4a.objects.ViewWrapper.lastId = 0;
        _activity = new ActivityWrapper(activityBA, "activity");
        anywheresoftware.b4a.Msgbox.isDismissing = false;
        if (BA.isShellModeRuntimeCheck(processBA)) {
			if (isFirst)
				processBA.raiseEvent2(null, true, "SHELL", false);
			processBA.raiseEvent2(null, true, "CREATE", true, "b4a.example.main", processBA, activityBA, _activity, anywheresoftware.b4a.keywords.Common.Density, mostCurrent);
			_activity.reinitializeForShell(activityBA, "activity");
		}
        initializeProcessGlobals();		
        initializeGlobals();
        
        BA.LogInfo("** Activity (main) Create, isFirst = " + isFirst + " **");
        processBA.raiseEvent2(null, true, "activity_create", false, isFirst);
		isFirst = false;
		if (this != mostCurrent)
			return;
        processBA.setActivityPaused(false);
        BA.LogInfo("** Activity (main) Resume **");
        processBA.raiseEvent(null, "activity_resume");
        if (android.os.Build.VERSION.SDK_INT >= 11) {
			try {
				android.app.Activity.class.getMethod("invalidateOptionsMenu").invoke(this,(Object[]) null);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

	}
	public void addMenuItem(B4AMenuItem item) {
		if (menuItems == null)
			menuItems = new java.util.ArrayList<B4AMenuItem>();
		menuItems.add(item);
	}
	@Override
	public boolean onCreateOptionsMenu(android.view.Menu menu) {
		super.onCreateOptionsMenu(menu);
        try {
            if (processBA.subExists("activity_actionbarhomeclick")) {
                Class.forName("android.app.ActionBar").getMethod("setHomeButtonEnabled", boolean.class).invoke(
                    getClass().getMethod("getActionBar").invoke(this), true);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (processBA.runHook("oncreateoptionsmenu", this, new Object[] {menu}))
            return true;
		if (menuItems == null)
			return false;
		for (B4AMenuItem bmi : menuItems) {
			android.view.MenuItem mi = menu.add(bmi.title);
			if (bmi.drawable != null)
				mi.setIcon(bmi.drawable);
            if (android.os.Build.VERSION.SDK_INT >= 11) {
				try {
                    if (bmi.addToBar) {
				        android.view.MenuItem.class.getMethod("setShowAsAction", int.class).invoke(mi, 1);
                    }
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			mi.setOnMenuItemClickListener(new B4AMenuItemsClickListener(bmi.eventName.toLowerCase(BA.cul)));
		}
        
		return true;
	}   
 @Override
 public boolean onOptionsItemSelected(android.view.MenuItem item) {
    if (item.getItemId() == 16908332) {
        processBA.raiseEvent(null, "activity_actionbarhomeclick");
        return true;
    }
    else
        return super.onOptionsItemSelected(item); 
}
@Override
 public boolean onPrepareOptionsMenu(android.view.Menu menu) {
    super.onPrepareOptionsMenu(menu);
    processBA.runHook("onprepareoptionsmenu", this, new Object[] {menu});
    return true;
    
 }
 protected void onStart() {
    super.onStart();
    processBA.runHook("onstart", this, null);
}
 protected void onStop() {
    super.onStop();
    processBA.runHook("onstop", this, null);
}
    public void onWindowFocusChanged(boolean hasFocus) {
       super.onWindowFocusChanged(hasFocus);
       if (processBA.subExists("activity_windowfocuschanged"))
           processBA.raiseEvent2(null, true, "activity_windowfocuschanged", false, hasFocus);
    }
	private class B4AMenuItemsClickListener implements android.view.MenuItem.OnMenuItemClickListener {
		private final String eventName;
		public B4AMenuItemsClickListener(String eventName) {
			this.eventName = eventName;
		}
		public boolean onMenuItemClick(android.view.MenuItem item) {
			processBA.raiseEventFromUI(item.getTitle(), eventName + "_click");
			return true;
		}
	}
    public static Class<?> getObject() {
		return main.class;
	}
    private Boolean onKeySubExist = null;
    private Boolean onKeyUpSubExist = null;
	@Override
	public boolean onKeyDown(int keyCode, android.view.KeyEvent event) {
        if (processBA.runHook("onkeydown", this, new Object[] {keyCode, event}))
            return true;
		if (onKeySubExist == null)
			onKeySubExist = processBA.subExists("activity_keypress");
		if (onKeySubExist) {
			if (keyCode == anywheresoftware.b4a.keywords.constants.KeyCodes.KEYCODE_BACK &&
					android.os.Build.VERSION.SDK_INT >= 18) {
				HandleKeyDelayed hk = new HandleKeyDelayed();
				hk.kc = keyCode;
				BA.handler.post(hk);
				return true;
			}
			else {
				boolean res = new HandleKeyDelayed().runDirectly(keyCode);
				if (res)
					return true;
			}
		}
		return super.onKeyDown(keyCode, event);
	}
	private class HandleKeyDelayed implements Runnable {
		int kc;
		public void run() {
			runDirectly(kc);
		}
		public boolean runDirectly(int keyCode) {
			Boolean res =  (Boolean)processBA.raiseEvent2(_activity, false, "activity_keypress", false, keyCode);
			if (res == null || res == true) {
                return true;
            }
            else if (keyCode == anywheresoftware.b4a.keywords.constants.KeyCodes.KEYCODE_BACK) {
				finish();
				return true;
			}
            return false;
		}
		
	}
    @Override
	public boolean onKeyUp(int keyCode, android.view.KeyEvent event) {
        if (processBA.runHook("onkeyup", this, new Object[] {keyCode, event}))
            return true;
		if (onKeyUpSubExist == null)
			onKeyUpSubExist = processBA.subExists("activity_keyup");
		if (onKeyUpSubExist) {
			Boolean res =  (Boolean)processBA.raiseEvent2(_activity, false, "activity_keyup", false, keyCode);
			if (res == null || res == true)
				return true;
		}
		return super.onKeyUp(keyCode, event);
	}
	@Override
	public void onNewIntent(android.content.Intent intent) {
        super.onNewIntent(intent);
		this.setIntent(intent);
        processBA.runHook("onnewintent", this, new Object[] {intent});
	}
    @Override 
	public void onPause() {
		super.onPause();
        if (_activity == null)
            return;
        if (this != mostCurrent)
			return;
		anywheresoftware.b4a.Msgbox.dismiss(true);
        BA.LogInfo("** Activity (main) Pause, UserClosed = " + activityBA.activity.isFinishing() + " **");
        if (mostCurrent != null)
            processBA.raiseEvent2(_activity, true, "activity_pause", false, activityBA.activity.isFinishing());		
        processBA.setActivityPaused(true);
        mostCurrent = null;
        if (!activityBA.activity.isFinishing())
			previousOne = new WeakReference<Activity>(this);
        anywheresoftware.b4a.Msgbox.isDismissing = false;
        processBA.runHook("onpause", this, null);
	}

	@Override
	public void onDestroy() {
        super.onDestroy();
		previousOne = null;
        processBA.runHook("ondestroy", this, null);
	}
    @Override 
	public void onResume() {
		super.onResume();
        mostCurrent = this;
        anywheresoftware.b4a.Msgbox.isDismissing = false;
        if (activityBA != null) { //will be null during activity create (which waits for AfterLayout).
        	ResumeMessage rm = new ResumeMessage(mostCurrent);
        	BA.handler.post(rm);
        }
        processBA.runHook("onresume", this, null);
	}
    private static class ResumeMessage implements Runnable {
    	private final WeakReference<Activity> activity;
    	public ResumeMessage(Activity activity) {
    		this.activity = new WeakReference<Activity>(activity);
    	}
		public void run() {
			if (mostCurrent == null || mostCurrent != activity.get())
				return;
			processBA.setActivityPaused(false);
            BA.LogInfo("** Activity (main) Resume **");
		    processBA.raiseEvent(mostCurrent._activity, "activity_resume", (Object[])null);
		}
    }
	@Override
	protected void onActivityResult(int requestCode, int resultCode,
	      android.content.Intent data) {
		processBA.onActivityResult(requestCode, resultCode, data);
        processBA.runHook("onactivityresult", this, new Object[] {requestCode, resultCode});
	}
	private static void initializeGlobals() {
		processBA.raiseEvent2(null, true, "globals", false, (Object[])null);
	}
    public void onRequestPermissionsResult(int requestCode,
        String permissions[], int[] grantResults) {
        for (int i = 0;i < permissions.length;i++) {
            Object[] o = new Object[] {permissions[i], grantResults[i] == 0};
            processBA.raiseEventFromDifferentThread(null,null, 0, "activity_permissionresult", true, o);
        }
            
    }

public anywheresoftware.b4a.keywords.Common __c = null;
public anywheresoftware.b4a.objects.LabelWrapper _weightlabel = null;
public anywheresoftware.b4a.objects.LabelWrapper _heightlabel = null;
public anywheresoftware.b4a.objects.LabelWrapper _resultlabel = null;
public anywheresoftware.b4a.objects.ImageViewWrapper _photoimageview = null;
public anywheresoftware.b4a.objects.EditTextWrapper _weightinput = null;
public anywheresoftware.b4a.objects.EditTextWrapper _heightinput = null;
public anywheresoftware.b4a.objects.ButtonWrapper _computebutton = null;
public anywheresoftware.b4a.objects.LabelWrapper _answertitlelabel = null;
public anywheresoftware.b4a.objects.LabelWrapper _resultdescriptionlabel = null;
public anywheresoftware.b4a.objects.PanelWrapper _photobgpanel = null;
public b4a.example.starter _starter = null;

public static boolean isAnyActivityVisible() {
    boolean vis = false;
vis = vis | (main.mostCurrent != null);
return vis;}
public static String  _activity_create(boolean _firsttime) throws Exception{
anywheresoftware.b4a.objects.drawable.ColorDrawable _computebuttonrgb = null;
 //BA.debugLineNum = 38;BA.debugLine="Sub Activity_Create(FirstTime As Boolean)";
 //BA.debugLineNum = 40;BA.debugLine="Activity.LoadLayout(\"pantalla1\")";
mostCurrent._activity.LoadLayout("pantalla1",mostCurrent.activityBA);
 //BA.debugLineNum = 43;BA.debugLine="Dim computeButtonRGB As ColorDrawable";
_computebuttonrgb = new anywheresoftware.b4a.objects.drawable.ColorDrawable();
 //BA.debugLineNum = 44;BA.debugLine="computeButtonRGB.Initialize(Colors.rgb(4,30,50),";
_computebuttonrgb.Initialize(anywheresoftware.b4a.keywords.Common.Colors.RGB((int) (4),(int) (30),(int) (50)),anywheresoftware.b4a.keywords.Common.PerXToCurrent((float) (2),mostCurrent.activityBA));
 //BA.debugLineNum = 45;BA.debugLine="ComputeButton.Background = computeButtonRGB";
mostCurrent._computebutton.setBackground((android.graphics.drawable.Drawable)(_computebuttonrgb.getObject()));
 //BA.debugLineNum = 46;BA.debugLine="End Sub";
return "";
}
public static String  _activity_pause(boolean _userclosed) throws Exception{
 //BA.debugLineNum = 53;BA.debugLine="Sub Activity_Pause (UserClosed As Boolean)";
 //BA.debugLineNum = 55;BA.debugLine="End Sub";
return "";
}
public static String  _activity_resume() throws Exception{
 //BA.debugLineNum = 49;BA.debugLine="Sub Activity_Resume";
 //BA.debugLineNum = 51;BA.debugLine="End Sub";
return "";
}
public static String  _computebutton_click() throws Exception{
double _weightvalue = 0;
double _heightvalue = 0;
double _result = 0;
 //BA.debugLineNum = 86;BA.debugLine="Sub ComputeButton_Click";
 //BA.debugLineNum = 87;BA.debugLine="Dim weightValue, heightValue, result As Double";
_weightvalue = 0;
_heightvalue = 0;
_result = 0;
 //BA.debugLineNum = 88;BA.debugLine="If (Not (ValidateWeight(WeightInput.Text)) Or Not";
if ((anywheresoftware.b4a.keywords.Common.Not(_validateweight(mostCurrent._weightinput.getText())) || anywheresoftware.b4a.keywords.Common.Not(_validateheight(mostCurrent._heightinput.getText())))) { 
 //BA.debugLineNum = 89;BA.debugLine="ToastMessageShow(\"Debes ingresar números, hasta";
anywheresoftware.b4a.keywords.Common.ToastMessageShow(BA.ObjectToCharSequence("Debes ingresar números, hasta 400kg y 260cm como máximo"),anywheresoftware.b4a.keywords.Common.False);
 //BA.debugLineNum = 90;BA.debugLine="Return";
if (true) return "";
 };
 //BA.debugLineNum = 92;BA.debugLine="weightValue = WeightInput.Text";
_weightvalue = (double)(Double.parseDouble(mostCurrent._weightinput.getText()));
 //BA.debugLineNum = 93;BA.debugLine="heightValue = HeightInput.Text";
_heightvalue = (double)(Double.parseDouble(mostCurrent._heightinput.getText()));
 //BA.debugLineNum = 94;BA.debugLine="result = weightValue/(Power(heightValue*0.01,2))";
_result = _weightvalue/(double)(anywheresoftware.b4a.keywords.Common.Power(_heightvalue*0.01,2));
 //BA.debugLineNum = 95;BA.debugLine="RenderResult(result)";
_renderresult(_result);
 //BA.debugLineNum = 96;BA.debugLine="End Sub";
return "";
}
public static String[]  _exactbodytypevalue(double _result) throws Exception{
String _bodytype = "";
String _comment = "";
String[] _resultarray = null;
 //BA.debugLineNum = 98;BA.debugLine="Sub ExactBodyTypeValue(result As Double) As String";
 //BA.debugLineNum = 99;BA.debugLine="Dim bodyType As String";
_bodytype = "";
 //BA.debugLineNum = 100;BA.debugLine="Dim comment As String";
_comment = "";
 //BA.debugLineNum = 102;BA.debugLine="If (result <= 18) Then";
if ((_result<=18)) { 
 //BA.debugLineNum = 103;BA.debugLine="bodyType = \"Bajo\"";
_bodytype = "Bajo";
 //BA.debugLineNum = 104;BA.debugLine="comment = \"Tienes un peso bajo, no deberías segu";
_comment = "Tienes un peso bajo, no deberías seguir el ejemplo de Toallín.";
 }else if((_result<=25)) { 
 //BA.debugLineNum = 106;BA.debugLine="bodyType = \"Normal\"";
_bodytype = "Normal";
 //BA.debugLineNum = 107;BA.debugLine="comment = \"Estás dentro del rango normal de peso";
_comment = "Estás dentro del rango normal de peso, ¡sigue así!";
 }else if((_result<=29.9)) { 
 //BA.debugLineNum = 109;BA.debugLine="bodyType = \"Sobrepeso\"";
_bodytype = "Sobrepeso";
 //BA.debugLineNum = 110;BA.debugLine="comment = \"Tienes sobrepeso, ¿qué diría Cartman";
_comment = "Tienes sobrepeso, ¿qué diría Cartman al respecto?";
 }else {
 //BA.debugLineNum = 112;BA.debugLine="bodyType = \"Obeso\"";
_bodytype = "Obeso";
 //BA.debugLineNum = 113;BA.debugLine="comment = \"Tienes obesidad, ser un Chef tiene su";
_comment = "Tienes obesidad, ser un Chef tiene sus desventajas...";
 };
 //BA.debugLineNum = 116;BA.debugLine="Dim resultArray(2) As String";
_resultarray = new String[(int) (2)];
java.util.Arrays.fill(_resultarray,"");
 //BA.debugLineNum = 117;BA.debugLine="resultArray(0) = bodyType";
_resultarray[(int) (0)] = _bodytype;
 //BA.debugLineNum = 118;BA.debugLine="resultArray(1) = comment";
_resultarray[(int) (1)] = _comment;
 //BA.debugLineNum = 120;BA.debugLine="Return resultArray";
if (true) return _resultarray;
 //BA.debugLineNum = 121;BA.debugLine="End Sub";
return null;
}
public static String  _globals() throws Exception{
 //BA.debugLineNum = 22;BA.debugLine="Sub Globals";
 //BA.debugLineNum = 26;BA.debugLine="Private WeightLabel As Label";
mostCurrent._weightlabel = new anywheresoftware.b4a.objects.LabelWrapper();
 //BA.debugLineNum = 27;BA.debugLine="Private HeightLabel As Label";
mostCurrent._heightlabel = new anywheresoftware.b4a.objects.LabelWrapper();
 //BA.debugLineNum = 28;BA.debugLine="Private ResultLabel As Label";
mostCurrent._resultlabel = new anywheresoftware.b4a.objects.LabelWrapper();
 //BA.debugLineNum = 29;BA.debugLine="Private PhotoImageView As ImageView";
mostCurrent._photoimageview = new anywheresoftware.b4a.objects.ImageViewWrapper();
 //BA.debugLineNum = 30;BA.debugLine="Private WeightInput As EditText";
mostCurrent._weightinput = new anywheresoftware.b4a.objects.EditTextWrapper();
 //BA.debugLineNum = 31;BA.debugLine="Private HeightInput As EditText";
mostCurrent._heightinput = new anywheresoftware.b4a.objects.EditTextWrapper();
 //BA.debugLineNum = 32;BA.debugLine="Private ComputeButton As Button";
mostCurrent._computebutton = new anywheresoftware.b4a.objects.ButtonWrapper();
 //BA.debugLineNum = 33;BA.debugLine="Private AnswerTitleLabel As Label";
mostCurrent._answertitlelabel = new anywheresoftware.b4a.objects.LabelWrapper();
 //BA.debugLineNum = 34;BA.debugLine="Private ResultDescriptionLabel As Label";
mostCurrent._resultdescriptionlabel = new anywheresoftware.b4a.objects.LabelWrapper();
 //BA.debugLineNum = 35;BA.debugLine="Private PhotoBgPanel As Panel";
mostCurrent._photobgpanel = new anywheresoftware.b4a.objects.PanelWrapper();
 //BA.debugLineNum = 36;BA.debugLine="End Sub";
return "";
}
public static String  _heightlabel_click() throws Exception{
 //BA.debugLineNum = 152;BA.debugLine="Sub HeightLabel_Click";
 //BA.debugLineNum = 153;BA.debugLine="HeightLabel = Sender";
mostCurrent._heightlabel.setObject((android.widget.TextView)(anywheresoftware.b4a.keywords.Common.Sender(mostCurrent.activityBA)));
 //BA.debugLineNum = 154;BA.debugLine="HeightInput.RequestFocus";
mostCurrent._heightinput.RequestFocus();
 //BA.debugLineNum = 155;BA.debugLine="End Sub";
return "";
}

public static void initializeProcessGlobals() {
    
    if (main.processGlobalsRun == false) {
	    main.processGlobalsRun = true;
		try {
		        main._process_globals();
starter._process_globals();
		
        } catch (Exception e) {
			throw new RuntimeException(e);
		}
    }
}public static String  _process_globals() throws Exception{
 //BA.debugLineNum = 16;BA.debugLine="Sub Process_Globals";
 //BA.debugLineNum = 20;BA.debugLine="End Sub";
return "";
}
public static String  _renderresult(double _result) throws Exception{
String[] _bodytypevalue = null;
 //BA.debugLineNum = 123;BA.debugLine="Sub RenderResult(result As Double)";
 //BA.debugLineNum = 126;BA.debugLine="Dim bodyTypeValue(2) As String";
_bodytypevalue = new String[(int) (2)];
java.util.Arrays.fill(_bodytypevalue,"");
 //BA.debugLineNum = 127;BA.debugLine="AnswerTitleLabel.Visible = True";
mostCurrent._answertitlelabel.setVisible(anywheresoftware.b4a.keywords.Common.True);
 //BA.debugLineNum = 128;BA.debugLine="ResultLabel.Visible = True";
mostCurrent._resultlabel.setVisible(anywheresoftware.b4a.keywords.Common.True);
 //BA.debugLineNum = 129;BA.debugLine="PhotoImageView.Visible = True";
mostCurrent._photoimageview.setVisible(anywheresoftware.b4a.keywords.Common.True);
 //BA.debugLineNum = 130;BA.debugLine="ResultDescriptionLabel.Visible = True";
mostCurrent._resultdescriptionlabel.setVisible(anywheresoftware.b4a.keywords.Common.True);
 //BA.debugLineNum = 131;BA.debugLine="PhotoBgPanel.Visible = True";
mostCurrent._photobgpanel.setVisible(anywheresoftware.b4a.keywords.Common.True);
 //BA.debugLineNum = 134;BA.debugLine="bodyTypeValue = ExactBodyTypeValue(result)";
_bodytypevalue = _exactbodytypevalue(_result);
 //BA.debugLineNum = 135;BA.debugLine="ResultLabel.Text = Round2(result,2)";
mostCurrent._resultlabel.setText(BA.ObjectToCharSequence(anywheresoftware.b4a.keywords.Common.Round2(_result,(int) (2))));
 //BA.debugLineNum = 136;BA.debugLine="ResultDescriptionLabel.Text = bodyTypeValue(1)";
mostCurrent._resultdescriptionlabel.setText(BA.ObjectToCharSequence(_bodytypevalue[(int) (1)]));
 //BA.debugLineNum = 137;BA.debugLine="UpdateResultImage(bodyTypeValue(0))";
_updateresultimage(_bodytypevalue[(int) (0)]);
 //BA.debugLineNum = 138;BA.debugLine="End Sub";
return "";
}
public static String  _updateresultimage(String _source) throws Exception{
String _finalsource = "";
 //BA.debugLineNum = 140;BA.debugLine="Sub UpdateResultImage( source As String)";
 //BA.debugLineNum = 142;BA.debugLine="Dim finalSource As String";
_finalsource = "";
 //BA.debugLineNum = 143;BA.debugLine="finalSource =  source.ToLowerCase() & \".jpg\" ' &:";
_finalsource = _source.toLowerCase()+".jpg";
 //BA.debugLineNum = 144;BA.debugLine="PhotoImageView.Bitmap = LoadBitmap(File.DirAssets";
mostCurrent._photoimageview.setBitmap((android.graphics.Bitmap)(anywheresoftware.b4a.keywords.Common.LoadBitmap(anywheresoftware.b4a.keywords.Common.File.getDirAssets(),_finalsource).getObject()));
 //BA.debugLineNum = 145;BA.debugLine="End Sub";
return "";
}
public static boolean  _validateheight(String _number) throws Exception{
 //BA.debugLineNum = 76;BA.debugLine="Sub ValidateHeight(Number As String) As Boolean";
 //BA.debugLineNum = 77;BA.debugLine="If (ValidatePositiveNumber(Number)) Then";
if ((_validatepositivenumber(_number))) { 
 //BA.debugLineNum = 78;BA.debugLine="If (Number <= 260) Then";
if (((double)(Double.parseDouble(_number))<=260)) { 
 //BA.debugLineNum = 79;BA.debugLine="Return True";
if (true) return anywheresoftware.b4a.keywords.Common.True;
 };
 };
 //BA.debugLineNum = 82;BA.debugLine="Return False";
if (true) return anywheresoftware.b4a.keywords.Common.False;
 //BA.debugLineNum = 83;BA.debugLine="End Sub";
return false;
}
public static boolean  _validatepositivenumber(String _number) throws Exception{
 //BA.debugLineNum = 58;BA.debugLine="Sub ValidatePositiveNumber(Number As String) As Bo";
 //BA.debugLineNum = 59;BA.debugLine="If (IsNumber(Number) And Number > 0) Then";
if ((anywheresoftware.b4a.keywords.Common.IsNumber(_number) && (double)(Double.parseDouble(_number))>0)) { 
 //BA.debugLineNum = 60;BA.debugLine="Return True";
if (true) return anywheresoftware.b4a.keywords.Common.True;
 };
 //BA.debugLineNum = 62;BA.debugLine="Return False";
if (true) return anywheresoftware.b4a.keywords.Common.False;
 //BA.debugLineNum = 63;BA.debugLine="End Sub";
return false;
}
public static boolean  _validateweight(String _number) throws Exception{
 //BA.debugLineNum = 66;BA.debugLine="Sub ValidateWeight(Number As String) As Boolean";
 //BA.debugLineNum = 67;BA.debugLine="If (ValidatePositiveNumber(Number)) Then";
if ((_validatepositivenumber(_number))) { 
 //BA.debugLineNum = 68;BA.debugLine="If (Number <= 400) Then";
if (((double)(Double.parseDouble(_number))<=400)) { 
 //BA.debugLineNum = 69;BA.debugLine="Return True";
if (true) return anywheresoftware.b4a.keywords.Common.True;
 };
 };
 //BA.debugLineNum = 72;BA.debugLine="Return False";
if (true) return anywheresoftware.b4a.keywords.Common.False;
 //BA.debugLineNum = 73;BA.debugLine="End Sub";
return false;
}
public static String  _weightlabel_click() throws Exception{
 //BA.debugLineNum = 148;BA.debugLine="Sub WeightLabel_Click";
 //BA.debugLineNum = 149;BA.debugLine="WeightLabel = Sender";
mostCurrent._weightlabel.setObject((android.widget.TextView)(anywheresoftware.b4a.keywords.Common.Sender(mostCurrent.activityBA)));
 //BA.debugLineNum = 150;BA.debugLine="WeightInput.RequestFocus";
mostCurrent._weightinput.RequestFocus();
 //BA.debugLineNum = 151;BA.debugLine="End Sub";
return "";
}
}
