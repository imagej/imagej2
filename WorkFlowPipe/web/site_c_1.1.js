cook=function(_1){
var _2=_1.replace(/&/g,"&amp;");
_2=_2.replace(/>/g,"&gt;");
_2=_2.replace(/</g,"&lt;");
return _2;
};
uncook=function(_3){
var _4=_3.replace(/&lt;/g,"<");
_4=_4.replace(/&gt;/g,">");
_4=_4.replace(/&amp;/g,"&");
return _4;
};
ua={isGecko:/Gecko/.test(navigator.userAgent),isIE:/MSIE/.test(navigator.userAgent),isKHTML:/Konqueror|Safari|KHTML/.test(navigator.userAgent),isOpera:/Opera/.test(navigator.userAgent),isSafari:/AppleWebKit/.test(navigator.appVersion),myUA:function(){
if(this.isIE){
return "IE";
}
if(this.isSafari){
return "Safari";
}
if(this.isGecko){
return "Gecko";
}
if(this.isKHTML){
return "KHTML";
}
if(this.isOpera){
return "Opera";
}
return "undefined";
}};
function reportAbuse(){
var _5=location.href;
location.href="mail"+"to:"+"pipes-pipeabuse"+"@"+"yahoo-inc.com?subject=Reporting abuse for Pipe: "+_5+"&body="+_5+"%0A Please state why this Pipe is abusive:";
}
function fbs_click(){
u=location.href;
t=document.title;
window.open("http://www.facebook.com/sharer.php?u="+encodeURIComponent(u)+"&t="+encodeURIComponent(t),"sharer","toolbar=0,status=0,width=626,height=436");
return false;
}
function buildURL(_6,_7){
if(!_7){
_7={};
}
var _8=[];
for(var _9 in _6){
var _a=_6[_9];
if(_a){
if(typeof (_a)=="function"){
continue;
}
_8.push(encodeURI(_9)+"="+encodeURIComponent(_a));
}
}
if(_7.nocachebust){
}else{
var _b=Math.random()*10000;
var _c=Math.floor(_b);
_8.push("_rnd="+_c);
}
if(_7.nocrumb){
}else{
_8.push(".crumb="+crumb);
}
_8.push("_out=json");
return _8.join("&");
}
function ajaxErrorHandler(_d){
maxwell.PipesEditor.SetStatus(_d,"Communication error");
}
function ajaxStop(_e){
return YAHOO.util.Connect.abort(_e,_e._cb);
}
function ajaxCall(_f,_10,ok,_12,_13,_14){
if(!_12){
_12=ajaxErrorHandler;
}
if(!_14){
_14={};
}
if(!_13){
_13="GET";
}
var url;
var _16;
_f=_f.replace(/\//g,".");
if(!_f.match(/^ajax/)){
_f="ajax."+_f;
}
_f="/web/"+_f;
if(_13=="GET"){
url=_f+"?"+buildURL(_10,_14);
_16=null;
}else{
url=_f;
_16=buildURL(_10,_14);
}
var _17={success:function(o){
if(o.responseText){
o.decodedResponse=null;
try{
eval("o.decodedResponse="+o.responseText);
}
catch(ex){
}
var _19=o.decodedResponse;
if(_19==null){
var _1a="Oops. System error: problem parsing response";
YAHOO.log(o.responseText);
_12(_1a);
return;
}
if(typeof (_19.ok)=="undefined"){
_12("Oops. System error: badly formed response");
YAHOO.log(_19);
return;
}
if(!_19.ok){
if(!_19.error){
_12(_19);
}else{
_12(_19.error);
}
return;
}
ok(_19);
}else{
var _1a="problem receiving response data";
_12(_1a);
}
},failure:function(o){
var _1c=o.statusText;
_12(_1c);
}};
var _1d=YAHOO.util.Connect.asyncRequest(_13,url,_17,_16);
_1d._cb=_17;
return _1d;
}
$JSON=function(o){
if(o.toJSONString){
return o.toJSONString();
}
ArrayToJSONString=function(o){
var a=["["],b,i,l=o.length,v;
for(i=0;i<l;i+=1){
v=o[i];
switch(typeof v){
case "undefined":
case "function":
case "unknown":
break;
default:
if(b){
a.push(",");
}
a.push(v===null?"null":$JSON(v));
b=true;
}
}
a.push("]");
return a.join("");
};
ObjectToJSONString=function(o){
var a=["{"],b,i,v;
for(i in o){
if(o.hasOwnProperty(i)){
v=o[i];
switch(typeof v){
case "undefined":
case "function":
case "unknown":
break;
default:
if(b){
a.push(",");
}
a.push($JSON(i),":",v===null?"null":$JSON(v));
b=true;
}
}
}
a.push("}");
return a.join("");
};
if(typeof (o.sort)=="function"){
return ArrayToJSONString(o);
}
return ObjectToJSONString(o);
};
Boolean.prototype.toJSONString=function(){
return String(this);
};
Date.prototype.toJSONString=function(){
function f(n){
return n<10?"0"+n:n;
}
return "\""+this.getFullYear()+"-"+f(this.getMonth()+1)+"-"+f(this.getDate())+"T"+f(this.getHours())+":"+f(this.getMinutes())+":"+f(this.getSeconds())+"\"";
};
Number.prototype.toJSONString=function(){
return isFinite(this)?String(this):"null";
};
String.prototype.parseJSON=function(){
try{
if(/^("(\\.|[^"\\\n\r])*?"|[,:{}\[\]0-9.\-+Eaeflnr-u \n\r\t])+?$/.test(this)){
return eval("("+this+")");
}
}
catch(e){
}
throw new SyntaxError("parseJSON");
};
(function(){
var m={"\b":"\\b","\t":"\\t","\n":"\\n","\f":"\\f","\r":"\\r","\"":"\\\"","\\":"\\\\"};
String.prototype.toJSONString=function(){
if(/["\\\x00-\x1f]/.test(this)){
return "\""+this.replace(/([\x00-\x1f\\"])/g,function(a,b){
var c=m[b];
if(c){
return c;
}
c=b.charCodeAt();
return "\\u00"+Math.floor(c/16).toString(16)+(c%16).toString(16);
})+"\"";
}
return "\""+this+"\"";
};
})();
YAHOO.namespace("pipes");
YAHOO.namespace("pipes.site");
YAHOO.pipes.site=function(){
var $D=YAHOO.util.Dom;
var $E=YAHOO.util.Event;
function loadComponent(_31,_32,_33){
r=$D.getRegion(_32);
var _34=$D.get(_32);
_34.innerHTML="<h1>&nbsp;</h1><div class=\"centerpreview\"></div>";
var _35=get_or_create("throbber");
$D.setStyle(_35,"display","block");
YAHOO.util.Connect.asyncRequest("GET",_31,{"success":onSuccess,"failure":onFailure,"argument":[_32,_33]});
$D.setXY(_35,[r.left,r.top+75]);
$D.setStyle(_35,"position","absolute");
var _36=r.bottom-r.top;
if(_36<52){
_36=52;
}
$D.setStyle(_35,"width",(r.right-r.left)+"px");
$D.setStyle(_35,"height",_36+"px");
}
function get_or_create(id){
var el=$D.get(id);
if(!el){
el=document.createElement("div");
el.id=id;
document.body.appendChild(el);
}
return el;
}
function onSuccess(o){
var _3a=o.argument[0];
var _3b=o.argument[1];
var _3c=$D.get(_3a);
_3c.innerHTML=o.responseText;
$D.setX("throbber","-10000");
$D.setStyle("throbber","display","none");
if(_3b){
_3b();
}
}
function onFailure(o){
var _3e=$D.get(component);
_3e.innerHTML="Error Fetching Data";
if(o.argument[1]){
o.argument[1]();
}
}
return {loadComponent:loadComponent,get_or_create:get_or_create};
}();
YAHOO.pipes.site.searchbox=function(){
var _3f="Search for Pipes...";
function handleOnAvailable(me){
var _41=this.getElementsByTagName("input")[0];
YAHOO.util.Event.addListener(_41,"focus",inputFocus);
YAHOO.util.Event.addListener(_41,"blur",inputBlur);
YAHOO.util.Event.addListener(this,"submit",inputSubmit);
if(_41.value.length===0||_41.value==_3f){
YAHOO.util.Dom.addClass(_41,"novalue");
_41.value=_3f;
}
}
function inputSubmit(e){
var _43=this.getElementsByTagName("input")[0];
if(_43.value.length===0||_43.value==_3f||_43.value==""){
YAHOO.util.Event.stopEvent(e);
}
}
function inputFocus(e){
if(YAHOO.util.Dom.hasClass(this,"novalue")){
YAHOO.util.Dom.removeClass(this,"novalue");
this.value="";
}
}
function inputBlur(e){
if(this.value.length===0){
YAHOO.util.Dom.addClass(this,"novalue");
this.value=_3f;
}
}
YAHOO.util.Event.onAvailable("searchbox",handleOnAvailable);
YAHOO.util.Event.onAvailable("searchboxrefine",handleOnAvailable);
}();
YAHOO.namespace("pipes.site.subscribe");
YAHOO.pipes.site.subscribe=function(_46){
this.display=false;
this.node=_46;
this.container=_46.getElementsByTagName("ul")[0];
if(!this.container){
return;
}
this.node.removeChild(this.container);
document.body.appendChild(this.container);
YAHOO.util.Event.addListener(_46,"click",this.trigger,this,true);
YAHOO.util.Event.addListener(_46,"mouseover",this.containerMouseOver,this,true);
YAHOO.util.Event.addListener(_46,"mouseout",this.containerMouseOut,this,true);
YAHOO.util.Event.addListener(this.container,"mouseover",this.containerMouseOver,this,true);
YAHOO.util.Event.addListener(this.container,"mouseout",this.containerMouseOut,this,true);
};
YAHOO.pipes.site.subscribe.prototype.trigger=function(e){
if(this.displayed){
this.overContainer=false;
this.close();
}else{
this.open();
}
};
YAHOO.pipes.site.subscribe.prototype.containerMouseOver=function(e){
this.overContainer=true;
};
YAHOO.pipes.site.subscribe.prototype.containerMouseOut=function(e){
this.overContainer=false;
if(!this.closeTimer){
var _4a=this;
this.closeTimer=window.setTimeout(function(){
_4a.close();
_4a.closeTimer=null;
},100);
}
};
YAHOO.pipes.site.subscribe.prototype.close=function(){
if(!this.displayed){
return;
}
if(!this.container){
return;
}
if(this.overContainer){
return;
}
this.displayed=false;
YAHOO.util.Dom.removeClass(this.node,"active");
this.container.style.display="none";
this.overContainer=false;
};
YAHOO.pipes.site.subscribe.prototype.open=function(){
if(this.displayed){
return;
}
if(!this.node){
return;
}
if(this.closeTimer){
window.clearTimeout(this.closeTimer);
this.closeTimer=null;
}
this.displayed=true;
var r=YAHOO.util.Dom.getRegion(this.node);
this.container.style.visibility="hidden";
this.container.style.display="block";
this.container.style.width="auto";
this.cleft=r.left;
this.cright=r.right;
this.ctop=r.bottom;
var _4c=this;
window.setTimeout(function(){
YAHOO.util.Dom.addClass(_4c.node,"active");
_4c.cleft-=2;
_4c.ctop+=4;
YAHOO.util.Dom.setXY(_4c.container,[_4c.cleft,_4c.ctop]);
_4c.container.style.visibility="visible";
},1);
};
YAHOO.namespace("pipes.site.thumb");
YAHOO.pipes.site.thumb=function(_4d,_4e,_4f){
var cv=document.createElement("canvas");
cv.setAttribute("width",_4e);
cv.setAttribute("height",_4f);
cv.style.width=_4e+"px";
cv.style.height=_4f+"px";
this.canvasw=_4e;
this.canvash=_4f;
this.min_mh=2;
this.min_mw=8;
this.base_mh=60;
this.base_mw=120;
this.border=4;
//do not want to append to a null
if(getElementById(_4d)!=null)
	document.getElementById(_4d).appendChild(cv);
if(typeof (G_vmlCanvasManager)!="undefined"){
this.isie=true;
G_vmlCanvasManager.initElement(cv);
}
this.ctx=cv.getContext("2d");
};
YAHOO.pipes.site.thumb.prototype.generate=function(def){
var _52=10000;
var _53=10000;
var _54=0;
var _55=0;
if(!def||!def.layout||!def.wires){
return;
}
for(var i=0;i<def.layout.length;i++){
var xy=def.layout[i].xy;
if(xy[0]<_52){
_52=xy[0];
}
if(xy[1]<_53){
_53=xy[1];
}
if(xy[0]>_55){
_55=xy[0];
}
if(xy[1]>_54){
_54=xy[1];
}
}
var _58=(_55-_52)+this.base_mw;
var _59=(_54-_53)+this.base_mh;
var _5a=(this.canvasw-2*this.border)/_58;
var _5b=(this.canvash-2*this.border)/_59;
var dx=_58-(this.canvasw/_5a);
var dy=_59-((this.canvash-2*this.border)/_5b);
_52+=dx/2;
_53+=dy/2;
var w=this.base_mw*_5a;
var h=this.base_mh*_5b;
if(w<this.min_mw){
w=this.min_mw;
}
if(h<this.min_mh){
h=this.min_mh;
}
for(i=0;i<def.layout.length;i++){
var m=def.layout[i];
var xy=m.xy;
xy[0]-=_52;
xy[1]-=_53;
xy[0]=Math.round(xy[0]*_5a);
xy[1]=Math.round(xy[1]*_5b);
this.drawModule(xy[0],xy[1],w,h,m["id"]);
}
for(i=0;i<def.wires.length;i++){
var w=def.wires[i];
var _61=w.tgt.moduleid;
var _62=w.tgt.id;
var _63=w.src.moduleid;
var _64=w.src.id;
this.line(_63,_61,_64,_62);
}
};
YAHOO.pipes.site.thumb.prototype.m=[];
YAHOO.pipes.site.thumb.prototype.isie=false;
YAHOO.pipes.site.thumb.prototype.drawModule=function(x,y,w,h,id){
var r=4;
var ctx=this.ctx;
var _6c=this.isie;
if(h<r*3){
h=r*3;
}
var _6d=h*0.2+r;
this.m[id]=[x,y,w,h];
function t(){
ctx.beginPath();
ctx.arc(x+r,y+r,r,Math.PI,3*Math.PI/2,false);
ctx.lineTo(x+w-r,y);
ctx.arc(x+w-r,y+r,r,-Math.PI/2,0,false);
ctx.lineTo(x+w,y+_6d);
ctx.lineTo(x,y+_6d);
ctx.lineTo(x,y+r);
}
function b(){
ctx.beginPath();
ctx.arc(x+r,y+h-r,r,Math.PI/2,Math.PI,false);
ctx.lineTo(x,y+_6d);
ctx.lineTo(x+w,y+_6d);
ctx.lineTo(x+w,y+h-r);
ctx.arc(x+w-r,y+h-r,r,0,Math.PI/2,false);
ctx.lineTo(x+r,y+h);
}
ctx.fillStyle="rgb(129,198,253)";
t();
ctx.fill();
ctx.fillStyle="rgb(255,255,250)";
b();
ctx.fill();
ctx.strokeStyle="rgb(40,146,253)";
t();
ctx.stroke();
b();
ctx.stroke();
};
YAHOO.pipes.site.thumb.prototype.line=function(m1,m2,id1,id2){
if(!this.m[m1]||!this.m[m2]){
return;
}
var p1=this.m[m1];
var p2=this.m[m2];
var xy1=[p1[0]+(p1[2]/2),p1[1]+(p1[3]/2)];
var xy2=[p2[0]+(p2[2]/2),p2[1]+(p2[3]/2)];
var _76="#a4a4a4";
var _77="#ececec";
if(id1=="_OUTPUT"){
xy1[1]=p1[1]+p1[3];
}
if(id2=="_INPUT"){
xy2[1]=p2[1];
_77="#63e4ff";
_76="#25a3fc";
}
this.drawLine(xy1[0],xy1[1],xy2[0],xy2[1],_77,_76);
};
YAHOO.pipes.site.thumb.prototype.drawLine=function(x,y,x2,y2,_7c,_7d){
var ctx=this.ctx;
var w=4;
ctx.lineWidth=w;
ctx.lineCap="round";
ctx.strokeStyle=_7d;
var _80=[];
_80[0]=[x,y];
_80[3]=[x2,y2];
var dy=Math.abs(y2-y)/2;
_80[1]=[x,y+dy];
_80[2]=[x2,y2-dy];
ctx.beginPath();
ctx.moveTo(_80[0][0],_80[0][1]);
ctx.bezierCurveTo(_80[1][0],_80[1][1],_80[2][0],_80[2][1],_80[3][0],_80[3][1]);
ctx.stroke();
w=2;
ctx.lineWidth=w;
ctx.lineCap="round";
ctx.strokeStyle=_7c;
ctx.beginPath();
ctx.moveTo(_80[0][0],_80[0][1]);
ctx.bezierCurveTo(_80[1][0],_80[1][1],_80[2][0],_80[2][1],_80[3][0],_80[3][1]);
ctx.stroke();
};
YAHOO.namespace("pipes.site.InlineTextEdit");
YAHOO.pipes.site.InlineTextEdit=function(_82,_83,_84,_85,_86,_87){
this.onchange=new YAHOO.util.CustomEvent("onchange",this,true);
this.oncancel=new YAHOO.util.CustomEvent("oncancel",this,true);
this.originalNode=_82;
this.multiline=_83;
this.emptytext=_84;
this.nobuttons=_85;
this.inparent=_86;
this.iname=_87;
this.enable();
};
YAHOO.pipes.site.InlineTextEdit.prototype.disable=function(){
result=YAHOO.util.Event.removeListener(this.originalNode,"mouseover",this.inviteEditing);
result=YAHOO.util.Event.removeListener(this.originalNode,"mouseout",this.hideEditing);
result=YAHOO.util.Event.removeListener(this.originalNode,"click",this.startEditing);
if(this.node){
result=YAHOO.util.Event.removeListener(this.input,"blur",this.checkBlur);
}
};
YAHOO.pipes.site.InlineTextEdit.prototype.enable=function(){
YAHOO.util.Event.addListener(this.originalNode,"mouseover",this.inviteEditing,this,true);
YAHOO.util.Event.addListener(this.originalNode,"mouseout",this.hideEditing,this,true);
YAHOO.util.Event.addListener(this.originalNode,"click",this.startEditing,this,true);
this.editing=false;
};
YAHOO.pipes.site.InlineTextEdit.prototype.remove=function(){
this.disable();
if(this.node&&this.node.parentNode){
this.node.parentNode.removeChild(this.node);
}
};
YAHOO.pipes.site.InlineTextEdit.prototype.init=function(){
var _88=YAHOO.util.Dom.getRegion(this.originalNode);
if(!this.node){
var _89="";
if(this.multiline){
_89="<textarea rows=5></textarea>";
}else{
if(this.iname){
_89="<input type=\"text\" name=\""+this.iname+"\" />";
}else{
_89="<input type=\"text\" />";
}
}
if(!this.nobuttons){
_89+="<div><ul><li><button>Save</button></li><li><button>Cancel</button></li></ul></div>";
}
this.node=document.createElement("div");
this.node.className="inlineedit";
this.node.setAttribute("class","inlineedit");
this.node.innerHTML=_89;
if(!this.nobuttons){
var _8a=this.node.getElementsByTagName("li");
this.ok=_8a[0].firstChild;
this.cancel=_8a[1].firstChild;
}
this.input=this.node.getElementsByTagName("textarea")[0];
if(!this.input){
this.input=this.node.getElementsByTagName("input")[0];
YAHOO.util.Event.addListener(this.input,"keydown",function(e){
var key=YAHOO.util.Event.getCharCode(e);
if(key==13){
this.inviteOk();
YAHOO.util.Event.stopEvent(e);
}
},this,true);
}
if(!this.inparent){
document.body.appendChild(this.node);
}else{
YAHOO.util.Dom.get(this.originalNode).parentNode.appendChild(this.node);
}
if(!this.nobuttons){
YAHOO.util.Event.addListener(this.ok,"click",this.inviteOk,this,true);
YAHOO.util.Event.addListener(this.cancel,"click",this.inviteCancel,this,true);
}
YAHOO.util.Event.addListener(this.input,"blur",this.checkBlur,this,true);
var _8d=["fontSize","fontWeight","fontFamily","lineHeight","color"];
for(var i in _8d){
YAHOO.util.Dom.setStyle(this.input,_8d[i],YAHOO.util.Dom.getStyle(this.originalNode,_8d[i]));
}
}
if(this.expandright){
var pe=this.originalNode.parentNode;
while(pe&&YAHOO.util.Dom.getStyle(pe,"display")!="block"){
pe=pe.parentNode;
}
if(!pe){
pe=document.body;
}
var r=YAHOO.util.Dom.getRegion(pe);
this.node.style.width=(r.right-_88.left)+"px";
}else{
this.node.style.width=(_88.right-_88.left)+"px";
}
if(this.multiline){
this.node.style.height="auto";
}else{
this.node.style.height=(_88.bottom-_88.top)+"px";
}
this.originalNode.style.visibility="hidden";
this.node.style.display="block";
YAHOO.util.Dom.setXY(this.node,[_88.left-1,_88.top]);
this.input.focus();
};
YAHOO.pipes.site.InlineTextEdit.prototype.inviteEditing=function(e){
if(!YAHOO.util.Dom.hasClass(this.originalNode,"invite")){
YAHOO.util.Dom.addClass(this.originalNode,"invite");
}
};
YAHOO.pipes.site.InlineTextEdit.prototype.hideEditing=function(e){
YAHOO.util.Dom.removeClass(this.originalNode,"invite");
};
YAHOO.pipes.site.InlineTextEdit.prototype.startEditing=function(e){
this.init();
var txt=this.originalNode.innerHTML;
if(txt.toLowerCase()==this.emptytext.toLowerCase()){
txt="";
}
this.input.value=uncook(txt);
};
YAHOO.pipes.site.InlineTextEdit.prototype.checkBlur=function(e){
var _96=this;
this.cancelBlur=window.setTimeout(function(){
_96.inviteCancel();
},200);
};
YAHOO.pipes.site.InlineTextEdit.prototype.stopEditing=function(e){
this.inviteCancel();
};
YAHOO.pipes.site.InlineTextEdit.prototype.inviteOk=function(e){
var val=this.input.value;
if(val&&val.length>0){
var _9a=this.originalNode.innerHTML;
this.originalNode.innerHTML=val;
this.onchange.fire(val,_9a,this.originalNode);
}
this.inviteCancel();
};
YAHOO.pipes.site.InlineTextEdit.prototype.inviteCancel=function(e){
if(this.cancelBlur){
window.clearTimeout(this.cancelBlur);
this.cancelBlur=null;
}
this.node.style.display="none";
this.originalNode.style.visibility="visible";
this.hideEditing();
this.oncancel.fire();
};
YAHOO.pipes.site.InlinePermalinkEdit=function(_9c,_9d,_9e,_9f){
if(!_9d||_9d.length==0){
if(confirm("You must first personalize *your* own pipe web address from your pipe page.\r\n\r\nDo you want to do that now?")){
window.location.href="/web/person.info?editwebaddress=1";
}
return;
}
this.node=_9c;
if(!_9e){
_9e="";
}
this.onchange=new YAHOO.util.CustomEvent("onchange",this,true);
this.originalUrl=_9c.innerHTML;
this.prefix=_9d;
this.suffix=this.originalUrl.substring(_9d.length,this.originalUrl.length);
_9c.innerHTML=_9d+"<span>"+this.suffix+"</span>";
this.edit=new YAHOO.pipes.site.InlineTextEdit(_9c.getElementsByTagName("span")[0],false,_9e,false,true);
this.edit.expandright=true;
this.edit.startEditing();
this.type=_9f;
this.edit.onchange.subscribe(function(_a0,_a1){
this.node.innerHTML=this.originalUrl;
this.edit=null;
this.onchange.fire(_a1[0],_a1[1],_a1[2]);
},this,true);
this.edit.oncancel.subscribe(function(_a2,_a3){
this.node.innerHTML=this.originalUrl;
this.edit=null;
},this,true);
};
YAHOO.namespace("pipes.site.InlineTagEdit");
YAHOO.pipes.site.InlineTagEdit=function(_a4,_a5){
this.onaddtag=new YAHOO.util.CustomEvent("onaddtag",this,true);
this.onremovetag=new YAHOO.util.CustomEvent("onremovetag",this,true);
this.list=_a4;
this.emptytext=_a5;
this.init();
};
YAHOO.pipes.site.InlineTagEdit.prototype.enhanceLi=function(li){
var _a7=document.createElement("img");
_a7.setAttribute("src","space.gif");
YAHOO.util.Dom.addClass(_a7,"tagremove");
YAHOO.util.Event.addListener(_a7,"click",this.removeTag,this,true);
li.appendChild(_a7);
};
YAHOO.pipes.site.InlineTagEdit.prototype.init=function(){
if(!this.node){
var lis=this.list.getElementsByTagName("DIV");
for(var i=0;i<lis.length;i++){
this.enhanceLi(lis[i]);
}
var _aa="<input size='"+(this.emptytext.length)+"'class='taginput' type='text' value='"+this.emptytext+"' /><img class='tagadd' src='space.gif'  />";
this.node=document.createElement("div");
this.node.className="show inlinetag";
this.node.setAttribute("class","show inlinetag");
this.node.innerHTML=_aa;
this.list.appendChild(this.node);
this.input=this.node.firstChild;
this.add=this.node.getElementsByTagName("img")[0];
YAHOO.util.Event.addListener(this.add,"click",this.addTag,this,true);
YAHOO.util.Event.addListener(this.input,"focus",this.inputfocus,this,true);
YAHOO.util.Event.addListener(this.input,"blur",this.inputblur,this,true);
YAHOO.util.Event.addListener(this.input,"keydown",this.keydown,this,true);
}
};
YAHOO.pipes.site.InlineTagEdit.prototype.keydown=function(e){
var key=YAHOO.util.Event.getCharCode(e);
if(key==13||key==32||key==9||key==44){
this.addTag();
YAHOO.util.Event.stopEvent(e);
}
};
YAHOO.pipes.site.InlineTagEdit.prototype.inputfocus=function(e){
var val=this.input.value;
if(val==this.emptytext){
this.input.value="";
}
YAHOO.util.Dom.addClass(this.input,"focus");
YAHOO.util.Dom.addClass(this.add,"focus");
};
YAHOO.pipes.site.InlineTagEdit.prototype.inputblur=function(e){
var val=this.input.value;
if(val&&val.length>0&&val!=this.emptytext){
var _b1=this;
this.blurtimer=window.setTimeout(function(){
_b1.doinputblur();
},250);
}else{
this.doinputblur();
}
};
YAHOO.pipes.site.InlineTagEdit.prototype.doinputblur=function(){
YAHOO.util.Dom.removeClass(this.input,"focus");
YAHOO.util.Dom.removeClass(this.add,"focus");
this.input.value=this.emptytext;
this.blurtimer=null;
};
YAHOO.pipes.site.InlineTagEdit.prototype.addTag=function(e){
var val=this.input.value;
if(this.blurtimer){
window.clearTimeout(this.blurtimer);
this.blurtimer=null;
}
if(val&&val.length>0&&val!=this.emptytext){
var li=document.createElement("div");
li.className="show";
li.setAttribute("class","show");
li.innerHTML="<a href='search?r=tag:"+val+"'>"+val+"</a>";
this.enhanceLi(li);
this.list.insertBefore(li,this.node);
this.onaddtag.fire(val,li,this);
}
this.input.value="";
this.input.focus();
};
YAHOO.pipes.site.InlineTagEdit.prototype.removeTag=function(e){
var el=YAHOO.util.Event.getTarget(e).previousSibling;
var val=el.innerText;
if(!val){
val=el.innerHTML.replace(/<[^>]+>/g,"");
}
var li=el.parentNode;
this.onremovetag.fire(val,li,this);
};
YAHOO.pipes.site.InlineTagEdit.prototype.dynTagCount=function(e){
var _ba=this.list.id+"count";
var _bb=parseInt(document.getElementById(_ba).innerHTML);
if(e=="added"){
_bb++;
}else{
_bb--;
}
document.getElementById(_ba).innerHTML=_bb.toString();
};
YAHOO.namespace("pipes.site.ajax");
YAHOO.pipes.site.ajax.buildOverlay=function(_bc){
var _bd=document.createElement("div");
YAHOO.util.Dom.addClass(_bd,"ajaxwait");
var id=_bc.getAttribute("id");
if(!id){
id="overparent"+Math.floor(Math.random()*1000);
_bc.setAttribute("id",id);
}
YAHOO.util.Event.onContentReady(id,function(){
YAHOO.util.Event.onContentReady(document.body,function(){
if(_bd.dontShow){
return;
}
var r=YAHOO.util.Dom.getRegion(_bc);
document.body.appendChild(_bd);
YAHOO.util.Dom.setXY(_bd,[r.left,r.top]);
_bd.style.width=(r.right-r.left)+"px";
_bd.style.height=(r.bottom-r.top)+"px";
_bd.style.visibility="visible";
});
});
return _bd;
};
YAHOO.pipes.site.ajax.removeOverlay=function(_c0){
_c0.dontShow=true;
if(_c0&&_c0.parentNode){
_c0.parentNode.removeChild(_c0);
}
};
YAHOO.pipes.site.ajax.Savename=function(_c1,_c2,_c3){
var _c4=YAHOO.pipes.site.ajax.buildOverlay(_c2[2]);
ajaxCall("pipe/updatename",{id:_c3,name:_c2[0]},function(_c5){
_c2[2].innerHTML=_c5.data;
YAHOO.pipes.site.ajax.removeOverlay(_c4);
},function(_c6){
YAHOO.pipes.site.ajax.removeOverlay(_c4);
_c2[2].innerHTML=_c2[1];
alert("Problem updating name: "+_c6);
},"POST");
};
YAHOO.pipes.site.ajax.Savedesc=function(_c7,_c8,_c9){
var _ca=YAHOO.pipes.site.ajax.buildOverlay(_c8[2]);
ajaxCall("pipe/updatedesc",{id:_c9,desc:_c8[0]},function(_cb){
_c8[2].innerHTML=_cb.data;
YAHOO.pipes.site.ajax.removeOverlay(_ca);
},function(_cc){
YAHOO.pipes.site.ajax.removeOverlay(_ca);
_c8[2].innerHTML=_c8[1];
alert("Problem updating description: "+_cc);
},"POST");
};
YAHOO.pipes.site.ajax.Addtag=function(_cd,_ce,_cf){
var _d0=YAHOO.pipes.site.ajax.buildOverlay(_ce[1]);
ajaxCall("pipe/addtag",{id:_cf,tag:_ce[0],namespace:"user"},function(_d1){
YAHOO.pipes.site.ajax.removeOverlay(_d0);
if(_ce[2]){
_ce[2].dynTagCount("added");
}
},function(_d2){
YAHOO.pipes.site.ajax.removeOverlay(_d0);
_ce[1].parentNode.removeChild(_ce[1]);
alert("Problem adding tag: "+_d2);
},"POST");
};
YAHOO.pipes.site.ajax.Removetag=function(_d3,_d4,_d5){
var _d6=YAHOO.pipes.site.ajax.buildOverlay(_d4[1]);
ajaxCall("pipe/removetag",{id:_d5,tag:_d4[0],namespace:"user"},function(_d7){
YAHOO.pipes.site.ajax.removeOverlay(_d6);
_d4[1].parentNode.removeChild(_d4[1]);
if(_d4[2]){
_d4[2].dynTagCount("removed");
}
},function(_d8){
YAHOO.pipes.site.ajax.removeOverlay(_d6);
alert("Problem removing tag: "+_d8);
},"POST");
};
YAHOO.pipes.site.ajax.Addfav=function(_d9,el,_db){
var _dc=YAHOO.pipes.site.ajax.buildOverlay(_d9);
ajaxCall("pipe/addfavorite",{id:_db},function(_dd){
YAHOO.util.Dom.removeClass(el,"favoriteoff");
YAHOO.util.Dom.addClass(el,"favoriteon");
YAHOO.pipes.site.ajax.removeOverlay(_dc);
},function(_de){
YAHOO.pipes.site.ajax.removeOverlay(_dc);
alert("Problem setting favorite tag: "+_de);
},"POST");
};
YAHOO.pipes.site.ajax.Removefav=function(_df,el,_e1){
var _e2=YAHOO.pipes.site.ajax.buildOverlay(_df);
ajaxCall("pipe/removefavorite",{id:_e1},function(_e3){
YAHOO.util.Dom.removeClass(el,"favoriteon");
YAHOO.util.Dom.addClass(el,"favoriteoff");
YAHOO.pipes.site.ajax.removeOverlay(_e2);
},function(_e4){
YAHOO.pipes.site.ajax.removeOverlay(_e2);
alert("Problem setting favorite tag: "+_e4);
},"POST");
};
YAHOO.pipes.site.ajax.Setuserwebpath=function(_e5,el,_e7){
var _e8=YAHOO.pipes.site.ajax.buildOverlay(_e5);
ajaxCall("user/updatewebpath",{path:_e7},function(_e9){
_e5.innerHTML=_e9.data;
YAHOO.pipes.site.ajax.removeOverlay(_e8);
},function(_ea){
YAHOO.pipes.site.ajax.removeOverlay(_e8);
alert("Problem updating your web path: "+_ea.message);
},"POST");
};
YAHOO.pipes.site.ajax.Setpipewebpath=function(_eb,el,_ed,_ee){
var _ef=YAHOO.pipes.site.ajax.buildOverlay(_eb);
ajaxCall("pipe/updatewebpath",{id:_ed,path:_ee},function(_f0){
_eb.innerHTML=_f0.data;
YAHOO.pipes.site.ajax.removeOverlay(_ef);
},function(_f1){
YAHOO.pipes.site.ajax.removeOverlay(_ef);
alert("Problem updating pipe path: "+_f1.message);
},"POST");
};
YAHOO.pipes.site.ajax.Pipeinfo=function(_f2,_f3){
var _f4=YAHOO.pipes.site.ajax.buildOverlay(_f2);
var _f5={};
for(var i=0;i<_f3.elements.length;i++){
var el=_f3.elements[i];
_f5[el.name]=el.value;
}
ajaxCall("pipe/info",_f5,function(_f8){
YAHOO.pipes.site.ajax.removeOverlay(_f4);
var d=document.createElement("div");
d.innerHTML=_f8.data;
_f2.innerHTML="";
_f2.appendChild(d);
if(ua.isSafari||ua.isIE||ua.isOpera){
YAHOO.util.Event.onContentReady(d,function(){
var _fa=d.getElementsByTagName("script");
for(var p=0,_fc=_fa.length;p<_fc;p++){
if(_fc!=_fa.length){
p=--p;
_fc=_fa.length;
}
var _fd=_fa[p];
eval(_fd.innerHTML);
}
});
}
},function(_fe){
YAHOO.pipes.site.ajax.removeOverlay(_f4);
if(_fe!="Communication error"&&_fe!="communication failure"){
alert("Problem running pipe: "+_fe);
}
},"GET",{nocrumb:true,nocachebust:true});
};