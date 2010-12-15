function buildURL(_1) {
	var _2 = [];
	for ( var _3 in _1) {
		var _4 = _1[_3];
		if (_4) {
			if (typeof (_4) == "function") {
				continue;
			}
			_2.push(encodeURI(_3) + "=" + encodeURIComponent(_4));
		}
	}
	var _5 = Math.random() * 10000;
	var _6 = Math.floor(_5);
	_2.push("rnd=" + _6);
	_2.push(".crumb=" + crumb);
	return _2.join("&");
}
function ajaxErrorHandler(_7) {
	maxwell.PipesEditor.SetStatus(_7, "Communication error");
}
function ajaxStop(_8) {
	return YAHOO.util.Connect.abort(_8, _8._cb);
}
function ajaxCall(ajaxRelativePath, jsonData, ok, errorHandlingFunction, _d) {
	if (!errorHandlingFunction) {
		errorHandlingFunction = ajaxErrorHandler;
	}
	if (!_d) {
		_d = "GET";
	}
	var _e;
	var _f;
	if (isV2) {
		jsonData["_v2"] = "true";
	}
	ajaxRelativePath = ajaxRelativePath.replace(/\//g, ".");
	if (!ajaxRelativePath.match(/^ajax/)) {
		ajaxRelativePath = "ajax." + ajaxRelativePath;
	}
	if (_d == "GET") {
		_e = ajaxRelativePath + "?" + buildURL(jsonData);
		_f = null;
	} else {
		_e = ajaxRelativePath;
		_f = buildURL(jsonData);
	}
	var _10 = {
		success : function(o) {
			if (o.responseText) {
				o.decodedResponse = null;
				try {
					eval("o.decodedResponse=" + o.responseText);
				} catch (ex) {
				}
				var _12 = o.decodedResponse;
				if (_12 == null) {
					var _13 = "Oops. System error: problem parsing response";
					YAHOO.log(o.responseText);
					errorHandlingFunction(_13);
					return;
				}
				if (typeof (_12.ok) == "undefined") {
					errorHandlingFunction("Oops. System error: badly formed response");
					YAHOO.log(_12);
					return;
				}
				if (!_12.ok) {
					if (!_12.error) {
						errorHandlingFunction(_12);
					} else {
						errorHandlingFunction(_12.error);
					}
					return;
				} else {
					if (_12.ok && _12.error) {
						errorHandlingFunction(_12.error);
					}
				}
				ok(_12);
			} else {
				var _13 = "problem receiving response data";
				errorHandlingFunction(_13);
			}
		},
		failure : function(o) {
			var _15 = o.statusText;
			errorHandlingFunction(_15);
		}
	};
	var _16 = YAHOO.util.Connect.asyncRequest(_d, _e, _10, _f);
	_16._cb = _10;
	return _16;
}
$JSON = function(o) {
	if (o.toJSONString) {
		return o.toJSONString();
	}
	ArrayToJSONString = function(o) {
		var a = [ "[" ], b, i, l = o.length, v;
		for (i = 0; i < l; i += 1) {
			v = o[i];
			switch (typeof v) {
			case "undefined":
			case "function":
			case "unknown":
				break;
			default:
				if (b) {
					a.push(",");
				}
				a.push(v === null ? "null" : $JSON(v));
				b = true;
			}
		}
		a.push("]");
		return a.join("");
	};
	ObjectToJSONString = function(o) {
		var a = [ "{" ], b, i, v;
		for (i in o) {
			if (o.hasOwnProperty(i)) {
				v = o[i];
				switch (typeof v) {
				case "undefined":
				case "function":
				case "unknown":
					break;
				default:
					if (b) {
						a.push(",");
					}
					a.push($JSON(i), ":", v === null ? "null" : $JSON(v));
					b = true;
				}
			}
		}
		a.push("}");
		return a.join("");
	};
	if (typeof (o.sort) == "function") {
		return ArrayToJSONString(o);
	}
	return ObjectToJSONString(o);
};
Boolean.prototype.toJSONString = function() {
	return String(this);
};
Date.prototype.toJSONString = function() {
	function f(n) {
		return n < 10 ? "0" + n : n;
	}
	return "\"" + this.getFullYear() + "-" + f(this.getMonth() + 1) + "-"
			+ f(this.getDate()) + "T" + f(this.getHours()) + ":"
			+ f(this.getMinutes()) + ":" + f(this.getSeconds()) + "\"";
};
Number.prototype.toJSONString = function() {
	return isFinite(this) ? String(this) : "null";
};
String.prototype.parseJSON = function() {
	try {
		if (/^("(\\.|[^"\\\n\r])*?"|[,:{}\[\]0-9.\-+Eaeflnr-u \n\r\t])+?$/
				.test(this)) {
			return eval("(" + this + ")");
		}
	} catch (e) {
	}
	throw new SyntaxError("parseJSON");
};
(function() {
	var m = {
		"\b" : "\\b",
		"\t" : "\\t",
		"\n" : "\\n",
		"\f" : "\\f",
		"\r" : "\\r",
		"\"" : "\\\"",
		"\\" : "\\\\"
	};
	String.prototype.toJSONString = function() {
		if (/["\\\x00-\x1f]/.test(this)) {
			return "\""
					+ this.replace(/([\x00-\x1f\\"])/g, function(a, b) {
						var c = m[b];
						if (c) {
							return c;
						}
						c = b.charCodeAt();
						return "\\u00" + Math.floor(c / 16).toString(16)
								+ (c % 16).toString(16);
					}) + "\"";
		}
		return "\"" + this + "\"";
	};
})();
var DumperIndent = 1;
var DumperIndentText = " ";
var DumperNewline = "<br/>";
var DumperObject = null;
var DumperMaxDepth = -1;
var DumperIgnoreStandardObjects = true;
var DumperProperties = null;
var DumperTagProperties = new Object();
function DumperGetArgs(a, _29) {
	var _2a = new Array();
	for ( var i = _29; i < a.length; i++) {
		_2a[_2a.length] = a[i];
	}
	return _2a;
}
function DumperPopup(o) {
	var w = window.open("about:blank");
	w.document.open();
	w.document.writeln("<HTML><BODY><PRE>");
	w.document.writeln(Dumper(o, DumperGetArgs(arguments, 1)));
	w.document.writeln("</PRE></BODY></HTML>");
	w.document.close();
}
function DumperAlert(o) {
	alert(Dumper(o, DumperGetArgs(arguments, 1)));
}
function DumperWrite(o, _30) {
	var _31 = 2;
	var _32 = DumperIndentText;
	var _33 = DumperGetArgs(arguments, _31);
	DumperIndentText = "&nbsp;";
	_30.innerHTML = Dumper(o, _33);
	DumperIndentText = _32;
}
function DumperPad(len) {
	var ret = "";
	for ( var i = 0; i < len; i++) {
		ret += DumperIndentText;
	}
	return ret;
}
function Dumper(o) {
	var _38 = 1;
	var _39 = DumperIndent;
	var ret = "";
	if (arguments.length > 1 && typeof (arguments[1]) == "number") {
		_38 = arguments[1];
		_39 = arguments[2];
		if (o == DumperObject) {
			return "[original object]";
		}
	} else {
		DumperObject = o;
		if (arguments.length > 1) {
			var _3b = arguments;
			var _3c = 1;
			if (typeof (arguments[1]) == "object") {
				_3b = arguments[1];
				_3c = 0;
			}
			for ( var i = _3c; i < _3b.length; i++) {
				if (DumperProperties == null) {
					DumperProperties = new Object();
				}
				DumperProperties[_3b[i]] = 1;
			}
		}
	}
	if (DumperMaxDepth != -1 && _38 > DumperMaxDepth) {
		return "...";
	}
	if (DumperIgnoreStandardObjects) {
		if (o == window || o == window.document) {
			return "[Ignored Object]";
		}
	}
	if (o == null) {
		ret = "[null]";
		return ret;
	}
	if (typeof (o) == "function") {
		ret = "[function]";
		return ret;
	}
	if (typeof (o) == "boolean") {
		ret = (o) ? "true" : "false";
		return ret;
	}
	if (typeof (o) == "string") {
		ret = "'" + o + "'";
		return ret;
	}
	if (typeof (o) == "number") {
		ret = o;
		return ret;
	}
	if (typeof (o) == "object") {
		if (typeof (o.length) == "number") {
			ret = "[";
			for ( var i = 0; i < o.length; i++) {
				if (i > 0) {
					ret += "," + DumperNewline + DumperPad(_39);
				} else {
					ret += DumperNewline + DumperPad(_39);
				}
				ret += Dumper(o[i], _38 + 1, _39 - 0 + DumperIndent);
			}
			if (i > 0) {
				ret += DumperNewline + DumperPad(_39 - DumperIndent);
			}
			ret += "]";
			return ret;
		} else {
			ret = "{";
			var _3e = 0;
			for (i in o) {
				if (o == DumperObject && DumperProperties != null
						&& DumperProperties[i] != 1) {
				} else {
					if (typeof (o[i]) != "unknown") {
						var _3f = true;
						if (typeof (o.tagName) != "undefined") {
							if (typeof (DumperTagProperties[o.tagName]) != "undefined") {
								_3f = false;
								for ( var p = 0; p < DumperTagProperties[o.tagName].length; p++) {
									if (DumperTagProperties[o.tagName][p] == i) {
										_3f = true;
										break;
									}
								}
							}
						}
						if (_3f) {
							if (_3e++ > 0) {
								ret += "," + DumperNewline + DumperPad(_39);
							} else {
								ret += DumperNewline + DumperPad(_39);
							}
							ret += "'"
									+ i
									+ "' => "
									+ Dumper(o[i], _38 + 1, _39 - 0 + i.length
											+ 6 + DumperIndent);
						}
					}
				}
			}
			if (_3e > 0) {
				ret += DumperNewline + DumperPad(_39 - DumperIndent);
			}
			ret += "}";
			return ret;
		}
	}
}
$namespace("maxwell.Util");
maxwell.Util.$ = function(_41) {
	return document.getElementById(_41);
};
maxwell.Util.ua = {
	isGecko : /Gecko/.test(navigator.userAgent),
	isIE : /MSIE/.test(navigator.userAgent),
	isKHTML : /Konqueror|Safari|KHTML/.test(navigator.userAgent),
	isOpera : /Opera/.test(navigator.userAgent),
	isSafari : /AppleWebKit/.test(navigator.appVersion),
	myUA : function() {
		if (this.isIE) {
			return "IE";
		}
		if (this.isSafari) {
			return "Safari";
		}
		if (this.isGecko) {
			return "Gecko";
		}
		if (this.isKHTML) {
			return "KHTML";
		}
		if (this.isOpera) {
			return "Opera";
		}
		return "undefined";
	}
};
maxwell.Util.newId = function(_42) {
	maxwell.Util.newId.seq++;
	return _42 + "-" + maxwell.Util.newId.seq;
};
maxwell.Util.newId.seq = 1;
maxwell.Util.setInputValue = function(_43, _44) {
	var tag = _43.tagName;
	if (!tag) {
		return;
	}
	tag = tag.toUpperCase();
	if (tag == "SELECT") {
		for ( var i = 0; i < _43.options.length; i++) {
			var o = _43.options[i];
			if (_44 == o.value) {
				_43.selectedIndex = i;
				return;
			}
		}
		_43.insertBefore(cn("option", {
			value : _44,
			selected : 1
		}, null, _44), _43.firstChild);
		return;
	} else {
		if (tag == "INPUT") {
			var _48 = _43.getAttribute("type").toUpperCase();
			if (_48 == "CHECKBOX") {
				if (_44 && _44 != "false" && _44 != "off") {
					_43.checked = true;
				}
				return;
			}
			if (_48 == "RADIO") {
				if (_44 == _43.value) {
					_43.checked = true;
				}
				return;
			}
			if (_43.setValue) {
				_43.setValue(_44);
			} else {
				_43.value = _44;
			}
		} else {
			if (tag == "TEXTAREA") {
				var _48 = _43.getAttribute("type").toUpperCase();
				if (_43.setValue) {
					_43.setValue(_44);
				} else {
					_43.value = _44;
				}
			} else {
				YAHOO.log("PANIC - bad node for setInputValue");
			}
		}
	}
};
maxwell.Util.toList = function(_49) {
	if (_49) {
		if (_49.sort) {
			return _49;
		} else {
			return [ _49 ];
		}
	} else {
		return [];
	}
};
maxwell.Util.cn = function(tag, _4b, _4c, _4d) {
	var _4e = document.createElement(tag);
	maxwell.Util.sn(_4e, _4b, _4c);
	if (_4d) {
		_4e.innerHTML = _4d;
	}
	return _4e;
};
maxwell.Util.$endsWith = function(_4f, _50) {
	if (_4f.length < _50.length) {
		return false;
	}
	if (_4f.substr(_4f.length - _50.length, _50.length) == _50) {
		return true;
	}
	return false;
};
maxwell.Util.indexOf = function(_51) {
	var _52 = 0;
	for ( var i in this) {
		if (this[i] == _51) {
			return _52;
		}
		_52++;
	}
	return -1;
};
maxwell.Util.sn = function(_54, _55, _56) {
	if (!_54) {
		return;
	}
	if (_55) {
		for ( var i in _55) {
			var _58 = _55[i];
			if (typeof (_58) == "function") {
				continue;
			}
			if (maxwell.Util.ua.isIE && i == "type"
					&& (_54.tagName == "INPUT" || _54.tagName == "SELECT")) {
				continue;
			}
			if (i == "className") {
				i = "class";
				_54.className = _58;
			}
			if (_58 !== _54.getAttribute(i)) {
				try {
					if (_58 === false) {
						_54.removeAttribute(i);
					} else {
						_54.setAttribute(i, _58);
					}
				} catch (err) {
					YAHOO.log("WARNING: Couldnt sn for " + _54.tagName
							+ ", attr " + i + ", val " + _58);
				}
			}
		}
	}
	if (_56) {
		for ( var i in _56) {
			if (typeof (_56[i]) == "function") {
				continue;
			}
			if (_54.style[i] != _56[i]) {
				_54.style[i] = _56[i];
			}
		}
	}
};
maxwell.Util.$centerXY = function(_59) {
	var _5a = YAHOO.util.Region.getRegion(_59);
	var _5b = [];
	_5b[0] = (_5a.left + _5a.right) / 2;
	_5b[1] = (_5a.top + _5a.bottom) / 2;
	return _5b;
};
maxwell.Util.$idCounter = 0;
maxwell.Util.$id = function(_5c) {
	if (typeof (_5c) == "string") {
		return _5c;
	}
	try {
		var nid = _5c.getAttribute("id");
		if (nid) {
			return nid;
		}
		var nid = _5c.className;
		if (!nid || nid.length == 0) {
			nid = _5c.nodeName;
		} else {
			nid = nid.replace(/\s+/g, "");
		}
		nid = nid + ($idCounter++);
		_5c.setAttribute("id", nid);
		return nid;
	} catch (ex) {
		YAHOO.log("WARNING: id failed for " + _5c);
	}
};
maxwell.Util.Cookie = function() {
};
maxwell.Util.Cookie.set = function(_5e, _5f) {
	var _60 = $JSON(_5f);
	try {
		document.cookie = _5e + "=" + _60 + "; path=/";
	} catch (e) {
		YAHOO.log("WARNING: Cannot set cookie");
		maxwell.Cookie.canNotSetCookies = true;
	}
};
maxwell.Util.Cookie.get = function(_61) {
	_61 = _61 + "=";
	var _62 = document.cookie.split(";");
	for ( var i = 0; i < _62.length; ++i) {
		var c = _62[i];
		var _65 = c.indexOf(_61);
		if (_65 > -1) {
			var _66 = c.substring(_61.length + _65, c.length);
			return JSON.parse(_66);
		}
	}
	return null;
};
maxwell.Util.SetCanvasRegionIE = function(_67, _68, top, lw, lh) {
	sn(_67, null, {
		left : _68 + "px",
		top : top + "px",
		width : lw + "px",
		height : lh + "px"
	});
	_67.getContext("2d").clearRect(0, 0, lw, lh);
	return _67;
};
maxwell.Util.SetCanvasRegionSafari = function(_6c, _6d, top, lw, lh) {
	var _71 = _6c.className;
	if (!_71) {
		_71 = _6c.getAttribute("class");
	}
	var _72 = cn("canvas", {
		className : _71,
		width : lw,
		height : lh
	}, {
		left : _6d + "px",
		top : top + "px"
	});
	var _73 = YAHOO.util.Event.getListeners(_6c);
	for ( var _74 in _73) {
		var l = _73[_74];
		YAHOO.util.Event.addListener(_72, l.type, l.fn, l.obj, l.adjust);
	}
	YAHOO.util.Event.purgeElement(_6c);
	_6c.parentNode.replaceChild(_72, _6c);
	return _72;
};
maxwell.Util.SetCanvasRegion = function(_76, _77, top, lw, lh) {
	sn(_76, {
		width : lw,
		height : lh
	}, {
		left : _77 + "px",
		top : top + "px"
	});
	return _76;
};
if (maxwell.Util.ua.isIE) {
	maxwell.Util.SetCanvasRegion = maxwell.Util.SetCanvasRegionIE;
} else {
	if (maxwell.Util.ua.isSafari || maxwell.Util.ua.isOpera) {
		maxwell.Util.SetCanvasRegion = maxwell.Util.SetCanvasRegionSafari;
	}
}
maxwell.Util.SetCanvasSize = function(_7b, lw, lh) {
	if (maxwell.Util.ua.isSafari || maxwell.Util.ua.isOpera) {
		var _7e = _7b.className;
		if (!_7e) {
			_7e = _7b.getAttribute("class");
		}
		var _7f = cn("canvas", {
			className : _7e,
			width : lw,
			height : lh
		});
		_7b.parentNode.replaceChild(_7f, _7b);
		return _7f;
	}
	sn(_7b, {
		width : lw,
		height : lh
	}, {
		width : lw + "px",
		height : lh + "px"
	});
	if (_7b.getContext) {
		_7b.getContext("2d").clearRect(0, 0, lw, lh);
	}
	return _7b;
};
maxwell.Util.$clone = function(_80) {
	if (typeof (_80) != "object") {
		return _80;
	}
	if (_80 == null) {
		return _80;
	}
	var _81 = new Object();
	for ( var i in _80) {
		_81[i] = $clone(_80[i]);
	}
	return _81;
};
maxwell.Util.cook = function(str) {
	var tmp = str.replace(/&/g, "&amp;");
	tmp = tmp.replace(/>/g, "&gt;");
	tmp = tmp.replace(/</g, "&lt;");
	return tmp;
};
maxwell.Util.uncook = function(str) {
	var tmp = str.replace(/&lt;/g, "<");
	tmp = tmp.replace(/&gt;/g, ">");
	tmp = tmp.replace(/&amp;/g, "&");
	return tmp;
};
maxwell.Util.jsObjects = [];
maxwell.Util.$bind = function(_87, obj) {
	obj._nid = $id(_87);
	_87.setAttribute("hasObj", "true");
	maxwell.Util.jsObjects[$id(_87)] = obj;
};
maxwell.Util.$unbindNode = function(_89) {
	var obj = $boundObject(_89);
	if (obj) {
		obj._nid = null;
	}
	_89.removeAttribute("hasObj");
	maxwell.Util.jsObjects[$id(_89)] = null;
	return obj;
};
maxwell.Util.$unbindChildren = function(_8b) {
	if (!_8b || _8b.nodeType != 1) {
		return;
	}
	if (_8b.childNodes && _8b.childNodes.length) {
		for ( var i = _8b.childNodes.length - 1; i >= 0; i--) {
			$unbindChildren(_8b.childNodes[i]);
		}
	}
	var obj = $unbindNode(_8b);
	if (obj) {
		if (obj.remove) {
			obj.remove();
		}
	}
};
maxwell.Util.$unbindObject = function(obj) {
	var _8f = $boundNode(obj);
	if (_8f) {
		_8f.removeAttribute("hasObj");
		maxwell.Util.jsObjects[$id(_8f)] = null;
	}
	obj._nid = null;
};
maxwell.Util.$boundNode = function(obj) {
	if (obj._nid) {
		return $(obj._nid);
	}
	return null;
};
maxwell.Util.$boundObject = function(_91) {
	if (_91.getAttribute("hasObj")) {
		return maxwell.Util.jsObjects[$id(_91)];
	}
	return null;
};
maxwell.Util.addShim = function(n, _93) {
	if (!YAHOO.util.Dom.getStyle(n, "zIndex")) {
		n.style.zIndex = 2;
	}
	var _94 = n.offsetHeight + 1;
	var _95 = n.offsetWidth + 1;
	var top = -1;
	var _97 = -1;
	var _98 = cn("iframe", {
		width : _95 + "px",
		height : _94 + "px"
	}, {
		filter : "alpha(opacity=0)",
		zIndex : 1,
		top : top + "px",
		left : _97 + "px",
		border : "0px",
		margin : "0px",
		padding : "0px",
		position : "absolute"
	});
	n.appendChild(_98);
	var _99 = function(_9a) {
		if (!_9a) {
			_9a = this;
		}
		var n = _9a.parentNode;
		if (!n) {
			return;
		}
		var _9c = n.offsetHeight + 1;
		var _9d = n.offsetWidth + 1;
		sn(_9a, {
			width : _9d + "px",
			height : _9c + "px"
		});
	};
	_98.rsize = _99;
	if (!_93) {
		YAHOO.util.Event.addListener(n, "resize", _99, _98, true);
	}
	return _98;
};
maxwell.Util.insertAfter = function(_9e, _9f) {
	var ib = _9f.nextSibling;
	if (ib == null) {
		_9f.parentNode.appendChild(_9e);
	} else {
		_9f.parentNode.insertBefore(_9e, ib);
	}
};
maxwell.Util.getInnerText = function(elt) {
	var _a2 = elt.innerText;
	if (!_a2) {
		_a2 = elt.innerHTML.replace(/<[^>]+>/g, "");
	}
	return _a2;
};
for ( var funckey in maxwell.Util) {
	var isdef = eval("typeof(" + funckey + ")");
	if (isdef == "undefined") {
		eval(funckey.substr(funckey.lastIndexOf(".") + 1) + "=maxwell.Util."
				+ funckey);
	}
}
$namespace("maxwell.DragDrop.Source");
maxwell.DragDrop.Source = function(id, _a4, _a5, _a6) {
	this.onStartDrag = new YAHOO.util.CustomEvent("onStartDrag", this, true);
	this.onEndDrag = new YAHOO.util.CustomEvent("onEndDrag", this, true);
	this.onDragging = new YAHOO.util.CustomEvent("onDragging", this, true);
	this.onAcquiredTargets = new YAHOO.util.CustomEvent("onAcquiredTargets",
			this, true);
	if (!_a6) {
		_a6 = {};
	}
	this.sourceId = id;
	this.pid = maxwell.Util.newId("ddsource");
	maxwell.DragDrop.Source.superclass.constructor
			.call(this, id, this.pid, _a6);
	if (!_a6.dragElId) {
	}
	if (!_a5) {
		_a5 = [ "unknown" ];
	} else {
		if (typeof (_a5) == "string") {
			_a5 = [ _a5 ];
		}
	}
	this.setDataTypes(_a5);
	this.setData(_a4);
	this.isTarget = false;
};
$extend(maxwell.DragDrop.Source, YAHOO.util.DDProxy);
maxwell.DragDrop.Source.prototype._computeTargets = function() {
	this.targets = {};
	var _a7 = 0;
	for ( var k = 0; k < this.dataTypes.length; k++) {
		var g = "datadd-" + this.dataTypes[k];
		for ( var j in YAHOO.util.DragDropMgr.ids[g]) {
			var dd = YAHOO.util.DragDropMgr.ids[g][j];
			if (dd.isTarget && dd.id != this.id) {
				this.targets[dd.id] = dd;
				_a7++;
			}
		}
	}
	this.onAcquiredTargets.fire(this, this.targets);
	return this.targets;
};
maxwell.DragDrop.Source.prototype.setData = function(_ac, _ad) {
	this.data = _ac;
	if (_ad) {
		this.setDataTypes(_ad);
	}
};
maxwell.DragDrop.Source.prototype.setDataTypes = function(_ae) {
	this.dataTypes = _ae;
};
maxwell.DragDrop.Source.prototype.getData = function() {
	return this.data;
};
maxwell.DragDrop.Source.prototype.getDataTypes = function() {
	return this.dataTypes;
};
maxwell.DragDrop.Source.prototype.hasDataType = function(_af) {
	if (this.dataTypes.indexOf) {
		return (this.dataTypes.indexOf(_af) >= 0);
	}
	for ( var i = 0; i < this.dataTypes.length; i++) {
		if (this.dataTypes[i] == _af) {
			return true;
		}
	}
	return false;
};
maxwell.DragDrop.Source.prototype.onDrag = function(_b1) {
	this.onDragging.fire(this, _b1);
};
maxwell.DragDrop.Source.prototype.startDrag = function() {
	YAHOO.util.DDM.mode = YAHOO.util.DDM.INTERSECT;
	if (!this.config.dragElId && !this.ddproxy) {
		if (this.dragElId) {
			$(this.dragElId).style.visibility = "hidden";
		}
		var _b2 = $(this.sourceId);
		this.ddproxy = _b2.cloneNode(true);
		sn(this.ddproxy, {
			id : this.pid
		}, {
			zIndex : 9999,
			width : _b2.offsetWidth + "px",
			height : _b2.offsetHeight + "px",
			position : "absolute",
			cursor : "move"
		});
		document.body.appendChild(this.ddproxy);
		this.dragElId = this.pid;
	}
	var _b3 = this._computeTargets();
	for ( var i = 0; i < this.dataTypes.length; i++) {
		this.addToGroup("datadd-" + this.dataTypes[i]);
	}
	for ( var _b5 in this.targets) {
		var t = this.targets[_b5];
		if (!t) {
			continue;
		}
		if (t.startSourceDrag) {
			t.startSourceDrag.fire(this);
		}
	}
	this.onStartDrag.fire(this);
};
maxwell.DragDrop.Source.prototype.endDrag = function(_b7) {
	YAHOO.util.DDM.mode = YAHOO.util.DDM.POINT;
	for ( var _b8 in this.targets) {
		var t = this.targets[_b8];
		if (!t) {
			continue;
		}
		if (t.endSourceDrag) {
			t.endSourceDrag.fire(this, _b7);
		}
	}
	for ( var i = 0; i < this.dataTypes.length; i++) {
		this.removeFromGroup("datadd-" + this.dataTypes[i]);
	}
	this.onEndDrag.fire(this);
};
maxwell.DragDrop.Source.prototype.onDragDrop = function(e, dds) {
	dds = this.sortDDs(dds);
	for ( var i = 0; i < dds.length; i++) {
		var t = dds[i];
	}
	for ( var i = 0; i < dds.length; i++) {
		var t = dds[i];
		if (t && t.onSourceDragDrop) {
			var xy = YAHOO.util.Event.getXY(e);
			t.onSourceDragDrop.fire(this, xy);
			return;
		}
	}
};
maxwell.DragDrop.Source.prototype.onDragEnter = function(e, dds) {
	for ( var i = 0; i < dds.length; i++) {
		var t = dds[i];
		if (t && t.onSourceDragEnter) {
			t.onSourceDragEnter.fire(this);
		}
	}
};
maxwell.DragDrop.Source.prototype.onDragOut = function(e, dds) {
	for ( var i = 0; i < dds.length; i++) {
		var t = dds[i];
		if (t && t.onSourceDragOut) {
			t.onSourceDragOut.fire(this);
		}
	}
};
maxwell.DragDrop.Source.prototype.onDragOver = function(e, dds) {
	for ( var i = 0; i < dds.length; i++) {
		var t = dds[i];
		if (t && t.onSourceDragOver) {
			t.onSourceDragOver.fire(this);
		}
	}
};
maxwell.DragDrop.Source.prototype.sortDDs = function(dds) {
	var _cd = function(aa, bb) {
		if (aa.id == bb.id) {
			return 0;
		}
		var a = $(aa.id);
		var b = $(bb.id);
		if (a && !b) {
			return 1;
		}
		if (b && !a) {
			return -1;
		}
		if (!a && !b) {
			return 0;
		}
		if (a.parentNode != b.parentNode) {
			if (YAHOO.util.Dom.isAncestor(a, b)) {
				return 1;
			}
			return -1;
		}
		var az = YAHOO.util.Dom.getStyle(a, "zIndex");
		var bz = YAHOO.util.Dom.getStyle(b, "zIndex");
		if (az && bz) {
			return (az - bz);
		}
		if (az) {
			return az;
		}
		if (bz) {
			return bz;
		}
		return 0;
	};
	dds.sort(_cd);
	return dds;
};
$namespace("maxwell.DragDrop.Target");
maxwell.DragDrop.Target = function(id, _d5, _d6) {
	this.startSourceDrag = new YAHOO.util.CustomEvent("startSourceDrag", this,
			true);
	this.endSourceDrag = new YAHOO.util.CustomEvent("endSourceDrag", this, true);
	this.onSourceDragDrop = new YAHOO.util.CustomEvent("onSourceDragDrop",
			this, true);
	this.onSourceDragEnter = new YAHOO.util.CustomEvent("onSourceDragEnter",
			this, true);
	this.onSourceDragOut = new YAHOO.util.CustomEvent("onSourceDragOut", this,
			true);
	this.onSourceDragOver = new YAHOO.util.CustomEvent("onSourceDragOver",
			this, true);
	var pid = maxwell.Util.newId("ddtarget");
	maxwell.DragDrop.Target.superclass.constructor.call(this, id, pid, _d6);
	if (!_d5) {
		_d5 = [ "unknown" ];
	} else {
		if (typeof (_d5) == "string") {
			_d5 = [ _d5 ];
		}
	}
	this.setDataTypes(_d5);
	this.isTarget = true;
};
$extend(maxwell.DragDrop.Target, YAHOO.util.DDTarget);
maxwell.DragDrop.Target.prototype.setDataTypes = function(_d8) {
	this.dataTypes = _d8;
	for ( var i = 0; i < _d8.length; i++) {
		this.addToGroup("datadd-" + _d8[i]);
	}
};
$namespace("maxwell.SimpleWindow");
maxwell.SimpleWindow = function(_da) {
	if (!_da) {
		_da = {};
	}
	if (!_da.type) {
		_da.type = "";
	}
	if (!_da.title) {
		_da.title = "";
	}
	if (!_da.style) {
		_da.style = null;
	}
	this.config = _da;
	this.id = maxwell.SimpleWindow.uniqueId(_da.id, _da.namespace);
	this.type = _da.type;
	this.node = cn("div", {
		className : "mod"
	}, _da.style);
	this.content = cn("div", {
		className : "content"
	});
	this.hdwrap = cn("div", {
		className : "hd"
	});
	this.bdwrap = cn("div", {
		className : "bd"
	});
	this.ftwrap = cn("div", {
		className : "ft"
	});
	this.hd = cn("div", {
		className : "inner"
	});
	this.hdwrap.appendChild(this.hd);
	this.bd = cn("div", {
		className : "inner"
	});
	this.bdwrap.appendChild(this.bd);
	this.ft = cn("div", {
		className : "inner"
	}, null, "<p></p>");
	this.ftwrap.appendChild(this.ft);
	this.content.appendChild(this.hdwrap);
	this.content.appendChild(this.bdwrap);
	this.content.appendChild(this.ftwrap);
	this.node.appendChild(this.content);
	this.shown = true;
	this.onmove = new YAHOO.util.CustomEvent("onmove", this);
	this.onmovestart = new YAHOO.util.CustomEvent("onmovestart", this);
	this.onmoveend = new YAHOO.util.CustomEvent("onmoveend", this);
	this.onremove = new YAHOO.util.CustomEvent("onremove", this);
	this.onhideshowbody = new YAHOO.util.CustomEvent("onhideshowbody", this);
	YAHOO.util.Event.onAvailable($id(this.node), function() {
		this.initDragDrop();
	}, this, true);
	this.initHeader();
	if (maxwell.Util.ua.isIE) {
		YAHOO.util.Event.onAvailable($id(this.node), function() {
			maxwell.Util.addShim(this.node);
			this.content.style.zIndex = 2;
		}, this, true);
	}
};
maxwell.SimpleWindow.uniqueIds = {};
maxwell.SimpleWindow.uniqueId = function(id, _dc) {
	if (!id) {
		id = maxwell.Util.newId("sw");
	}
	if (!_dc) {
		_dc = "default";
	}
	if (!maxwell.SimpleWindow.uniqueIds[_dc]) {
		maxwell.SimpleWindow.uniqueIds[_dc] = {};
	}
	if (!maxwell.SimpleWindow.uniqueIds[_dc][id]) {
		maxwell.SimpleWindow.uniqueIds[_dc][id] = 1;
		return id;
	}
	YAHOO.log("WARNING: ID collision in modules");
	return maxwell.SimpleWindow.uniqueId();
};
maxwell.SimpleWindow.prototype.windowConfig = {
	closebutton : true,
	hidebutton : true,
	helpbutton : true
};
maxwell.SimpleWindow.prototype.initDragDrop = function() {
	var id = $id(this.node);
	this.dd = new YAHOO.util.DD(id, "ddgroup-" + id);
	var _de = this;
	this.dd.onDrag = function(_df) {
		var _e0 = YAHOO.util.Event.getXY(_df);
		var _e1 = _e0[0] - this.lastxy[0];
		var _e2 = _e0[1] - this.lastxy[1];
		_de.XY[0] += _e1;
		_de.XY[1] += _e2;
		this.lastxy = _e0;
		_de.fireOnMove(_de.XY, [ _e1, _e2 ]);
	};
	this.dd.startDrag = function(x, y) {
		if (maxwell.Util.ua.isIE) {
			document.body.ondrag = function() {
				return false;
			};
			document.body.onselectstart = function() {
				return false;
			};
		}
		_de.updateXY();
		this.lastxy = [ x, y ];
		_de.onmovestart.fire();
	};
	this.dd.endDrag = function(_e5) {
		if (maxwell.Util.ua.isIE) {
			document.body.ondrag = null;
			document.body.onselectstart = null;
		}
		_de.onmoveend.fire();
	};
	this.dd.addInvalidHandleType("INPUT");
	this.dd.addInvalidHandleType("SELECT");
	this.dd.addInvalidHandleType("TEXTAREA");
	this.dd.addInvalidHandleClass("buttons");
	this.dd.addInvalidHandleClass("close");
	this.dd.addInvalidHandleClass("hide");
	this.dd.addInvalidHandleClass("show");
};
maxwell.SimpleWindow.prototype.fireOnMove = function(_e6, _e7) {
	this.onmove.fire(_e6, _e7);
};
maxwell.SimpleWindow.prototype.setXY = function(_e8) {
	this.node.style.left = _e8[0] + "px";
	this.node.style.top = _e8[1] + "px";
	this.XY = _e8;
	this.fireOnMove(_e8);
};
maxwell.SimpleWindow.prototype.updateXY = function() {
	this.XY = [ this.node.offsetLeft, this.node.offsetTop ];
	return this.XY;
};
maxwell.SimpleWindow.prototype.getXY = function() {
	if (!this.XY) {
		return this.updateXY();
	}
	return this.XY;
};
maxwell.SimpleWindow.prototype.fireOnHideShowBody = function(_e9) {
	this.onhideshowbody.fire(_e9);
};
maxwell.SimpleWindow.prototype.fireOnRemove = function() {
	this.onremove.fire();
};
maxwell.SimpleWindow.prototype.initHeader = function() {
	this.title = cn("div", {
		className : "title"
	}, null, this.config.title);
	this.buttons = cn("ul", {
		className : "buttons"
	});
	if (this.windowConfig.helpbutton) {
		this.helpbutton = cn("li", {
			className : "help"
		});
		this.buttons.appendChild(this.helpbutton);
		YAHOO.util.Event.addListener(this.helpbutton, "click", this.help, this,
				true);
	}
	if (this.windowConfig.hidebutton) {
		this.hide = cn("li", {
			className : "hide"
		});
		this.buttons.appendChild(this.hide);
		YAHOO.util.Event.addListener(this.hd, "dblclick", this.hideshowbd,
				this, true);
		YAHOO.util.Event.addListener(this.hide, "click", this.hideshowbd, this,
				true);
	}
	if (this.windowConfig.closebutton) {
		var del = cn("li", {
			className : "close"
		});
		this.buttons.appendChild(del);
		YAHOO.util.Event.addListener(del, "click", this.remove, this, true);
	}
	this.hd.appendChild(this.title);
	this.hd.appendChild(this.buttons);
};
maxwell.SimpleWindow.prototype.setTitle = function(_eb) {
	if (!_eb.tagName) {
		this.title.innerHTML = _eb;
	} else {
		this.title.appendChild(_eb);
	}
};
maxwell.SimpleWindow.prototype.setBody = function(_ec) {
	if (!_ec.tagName) {
		this.bd.innerHTML = _ec;
	} else {
		this.bd.appendChild(_ec);
	}
};
maxwell.SimpleWindow.prototype.setFooter = function(_ed) {
	if (!_ed.tagName) {
		this.ft.innerHTML = _ed;
	} else {
		this.ft.appendChild(_ed);
	}
};
maxwell.SimpleWindow.prototype.remove = function() {
	if (this.removed) {
		return;
	}
	this.removed = true;
	if (this.dd) {
		this.dd.unreg();
	}
	if (this.node.parentNode) {
		this.node.parentNode.removeChild(this.node);
	}
	this.fireOnRemove();
};
maxwell.SimpleWindow.prototype.hideshowbd = function(e, obj) {
	if (this.shownbd) {
		this.hidebd();
	} else {
		this.showbd();
	}
};
maxwell.SimpleWindow.prototype.hidebd = function(e, obj) {
	if (this.bd && this.bd.style) {
		this.bd.style.display = "none";
		if (this.hide) {
			YAHOO.util.Dom.replaceClass(this.hide, "show", "hide");
		}
	}
	this.shownbd = false;
	if (this.node.offsetWidth) {
		this.resize(this.hdwrap.offsetWidth - 1);
	}
	this.fireOnHideShowBody(false);
	var _f2 = this;
	window.setTimeout(function() {
		_f2.fireOnMove(_f2.updateXY());
	}, 1);
};
maxwell.SimpleWindow.prototype.showbd = function(e, obj) {
	if (this.bd && this.bd.style) {
		this.bd.style.display = "block";
		if (this.hide) {
			YAHOO.util.Dom.replaceClass(this.hide, "hide", "show");
		}
	}
	this.shownbd = true;
	if (this.node.offsetWidth) {
		this.resize(this.hdwrap.offsetWidth + 1);
	}
	this.fireOnHideShowBody(true);
	var _f5 = this;
	window.setTimeout(function() {
		_f5.fireOnMove(_f5.updateXY());
	}, 1);
};
maxwell.SimpleWindow.prototype.addShim = function(_f6) {
	var n = _f6.content;
	n.style.zIndex = 2;
	var _f8 = YAHOO.util.Region.getRegion(n);
	var _f9 = (_f8.bottom - _f8.top);
	var _fa = (_f8.right - _f8.left);
	var top = 0;
	var _fc = 0;
	_f6.shim = cn("iframe", {
		width : _fa + "px",
		height : _f9 + "px"
	}, {
		filter : "alpha(opacity=0)",
		zIndex : 1,
		top : top + "px",
		left : _fc + "px",
		border : "0px",
		margin : "0px",
		padding : "0px",
		position : "absolute"
	});
	n.parentNode.appendChild(_f6.shim);
	YAHOO.util.Event.addListener(n, "resize", _f6.shimResize, _f6, true);
};
maxwell.SimpleWindow.prototype.shimResize = function() {
	var n = this.content;
	var _fe = n.offsetHeight;
	var _ff = n.offsetWidth;
	this.clearedForResize = null;
	sn(this.shim, {
		width : _ff + "px",
		height : _fe + "px"
	});
};
maxwell.SimpleWindow.prototype.resize = function(w, h) {
	if (w) {
		if (typeof (w) == "number") {
			w = w + "px";
		}
		this.bdwrap.style.width = w;
		this.hdwrap.style.width = w;
		this.ftwrap.style.width = w;
		this.node.style.width = w;
		this.content.style.width = w;
	}
	if (h) {
		if (typeof (h) == "number") {
			h = h + "px";
		}
		this.bd.style.height = h;
	}
};
maxwell.SimpleWindow.prototype.help = function() {
};
$namespace("maxwell.Wire");
wcount = 0;
maxwell.Wire = function(src, tgt, con) {
	this.id = "_w" + wcount;
	wcount++;
	this.src = src;
	this.tgt = tgt;
	this.rconfig = $clone(this.src.getRenderStyle());
	this.config = con;
	if (!this.config) {
		this.config = {};
	}
	this.onmove = new YAHOO.util.CustomEvent("onmove", this);
	this.onremove = new YAHOO.util.CustomEvent("onremove", this);
	var _105 = "wire";
	if (this.config.className) {
		_105 = _105 + " " + this.config.className;
	}
	this.node = cn("canvas", {
		className : _105
	});
	if (this.src && this.src.block && this.src.block.container) {
		this.src.block.container.node.appendChild(this.node);
	} else {
		if (this.tgt && this.tgt.block && this.tgt.block.container) {
			this.tgt.block.container.node.appendChild(this.node);
		} else {
			YAHOO.log("WARNING: manually determining wire container");
			var p = this.src.block.node.offsetParent;
			if (!p) {
				p = this.tgt.block.node.offsetParent;
			}
			while (p
					&& !(p.tagName == "DIV" && YAHOO.util.Dom.hasClass(p,
							"editcontainer"))) {
				p = p.offsetParent;
			}
			if (!p) {
				p = document.body;
			}
			p.appendChild(this.node);
		}
	}
	if (typeof (G_vmlCanvasManager) != "undefined") {
		G_vmlCanvasManager.initElement(this.node);
	}
	if (this.node.getContext) {
		this.ctxt = this.node.getContext("2d");
	}
	this.midxy = [ 0, 0 ];
	this.updateXY();
	this.redraw();
	this.src.onmove.subscribe(this.redraw, this, true);
	this.tgt.onmove.subscribe(this.redraw, this, true);
	if (this.src.onstylechange) {
		this.src.onstylechange.subscribe(this.updatestyle, this, true);
	}
	if (this.tgt.onstylechange) {
		this.tgt.onstylechange.subscribe(this.updatestyle, this, true);
	}
	if (this.tgt.addWire && this.src.addWire) {
		this.src.addWire(this);
		this.tgt.addWire(this);
	}
	YAHOO.util.Event.addListener(window, "resize", this.onwindowresize, this,
			true);
};
maxwell.Wire.prototype.updatestyle = function(type, args, me) {
	this.rconfig = $clone(args[0].getRenderStyle());
	this.redraw();
};
maxwell.Wire.prototype.updateXY = function() {
	if (this.src.updateXY) {
		this.src.updateXY();
	}
	if (this.tgt.updateXY) {
		this.tgt.updateXY();
	}
};
maxwell.Wire.prototype.remove = function() {
	if (this.removed) {
		return;
	}
	this.onremove.fire(this);
	this.removed = true;
	this.node.parentNode.removeChild(this.node);
	this.src.onmove.unsubscribe(this.redraw, this);
	this.tgt.onmove.unsubscribe(this.redraw, this);
	if (this.src.onstylechange) {
		this.src.onstylechange.unsubscribe(this.updatestyle, this);
	}
	if (this.tgt.onstylechange) {
		this.tgt.onstylechange.unsubscribe(this.updatestyle, this);
	}
	YAHOO.util.Event
			.removeListener(window, "resize", this.onwindowresize, this);
};
maxwell.Wire.prototype.redraw = function() {
	if (!this.node.parentNode) {
		return;
	}
	var p1 = this.src.getXY();
	var p2 = this.tgt.getXY();
	p1 = [ p1[0], p1[1] ];
	p2 = [ p2[0], p2[1] ];
	var _10c = 100;
	var _10d = Math.sqrt(Math.pow(p1[0] - p2[0], 2)
			+ Math.pow(p1[1] - p2[1], 2));
	if (_10d < _10c) {
		_10c = _10d / 2;
	}
	var d1 = [ this.src.direction[0] * _10c, this.src.direction[1] * _10c ];
	var d2 = [ this.tgt.direction[0] * _10c, this.tgt.direction[1] * _10c ];
	var _110 = [];
	_110[0] = p1;
	_110[1] = [ p1[0] + d1[0], p1[1] + d1[1] ];
	_110[2] = [ p2[0] + d2[0], p2[1] + d2[1] ];
	_110[3] = p2;
	var min = [ p1[0], p1[1] ];
	var max = [ p1[0], p1[1] ];
	for ( var i = 1; i < _110.length; i++) {
		var p = _110[i];
		if (p[0] < min[0]) {
			min[0] = p[0];
		}
		if (p[1] < min[1]) {
			min[1] = p[1];
		}
		if (p[0] > max[0]) {
			max[0] = p[0];
		}
		if (p[1] > max[1]) {
			max[1] = p[1];
		}
	}
	var _115 = [ 4, 4 ];
	min[0] = min[0] - _115[0];
	min[1] = min[1] - _115[1];
	max[0] = max[0] + _115[0];
	max[1] = max[1] + _115[1];
	var lw = Math.abs(max[0] - min[0]);
	var lh = Math.abs(max[1] - min[1]);
	this.node = maxwell.Util.SetCanvasRegion(this.node, min[0], min[1], lw, lh);
	var ctxt = this.node.getContext("2d");
	for ( var i = 0; i < _110.length; i++) {
		_110[i][0] = _110[i][0] - min[0];
		_110[i][1] = _110[i][1] - min[1];
	}
	var _119 = this.rconfig;
	ctxt.lineCap = _119.bordercap;
	ctxt.strokeStyle = _119.bordercolor;
	ctxt.lineWidth = _119.width + _119.borderwidth * 2;
	ctxt.beginPath();
	ctxt.moveTo(_110[0][0], _110[0][1]);
	ctxt.bezierCurveTo(_110[1][0], _110[1][1], _110[2][0], _110[2][1],
			_110[3][0], _110[3][1]);
	ctxt.stroke();
	ctxt.lineCap = _119.cap;
	ctxt.strokeStyle = _119.color;
	ctxt.lineWidth = _119.width;
	ctxt.beginPath();
	ctxt.moveTo(_110[0][0], _110[0][1]);
	ctxt.bezierCurveTo(_110[1][0], _110[1][1], _110[2][0], _110[2][1],
			_110[3][0], _110[3][1]);
	ctxt.stroke();
};
maxwell.Wire.prototype.fireOnMove = function() {
	if (!this.clearedForMove) {
		var self = this;
		if (this.moveTimeout) {
			window.clearTimeout(this.moveTimeout);
		}
		this.moveTimeout = window.setTimeout(function() {
			self.moveTimeout = null;
			self.clearedForMove = true;
		}, 1000);
		return;
	}
	this.midxy = $clone(this.tgt.getXY());
	this.clearedForMove = false;
	this.onmove.fire(this.midxy);
};
maxwell.Wire.prototype.findBezierPoint = function(_11b, ptB0, ptB1, ptB2, ptB3) {
	var _120 = 100;
	var _121 = 1 / _120;
	var t = _121 * _11b;
	var fW = 1 - t;
	var fW2 = fW * fW;
	var t2 = t * t;
	var fA = fW * fW2;
	var fB = 3 * t * fW2;
	var fC = 3 * t2 * fW;
	var fD = t2 * t;
	var fX = fA * ptB0[0] + fB * ptB1[0] + fC * ptB2[0] + fD * ptB3[0];
	var fY = fA * ptB0[1] + fB * ptB1[1] + fC * ptB2[1] + fD * ptB3[1];
	return [ fX, fY ];
};
maxwell.Wire.prototype.getTarget = function() {
	if (this.src.id == "_OUTPUT") {
		return this.tgt;
	}
	if (this.tgt.id == "_INPUT") {
		return this.tgt;
	}
	return this.src;
};
maxwell.Wire.prototype.getSource = function() {
	if (this.src.id == "_OUTPUT") {
		return this.src;
	}
	if (this.tgt.id == "_OUTPUT") {
		return this.tgt;
	}
	return this.src;
};
$namespace("maxwell.Block");
maxwell.Block = function(_12c) {
	if (!_12c) {
		_12c = {};
	}
	this.terminals = [];
	this.id = _12c.id ? _12c.id : maxwell.Util.newId("block");
	this.node = cn("div", {
		className : "block"
	});
	this.onmove = new YAHOO.util.CustomEvent("onmove", this);
	this.onremove = new YAHOO.util.CustomEvent("onremove", this);
	this.onnewterminal = new YAHOO.util.CustomEvent("onnewterminal", this);
	this.onremoveterminal = new YAHOO.util.CustomEvent("onremoveterminal", this);
	this.dd = new YAHOO.util.DD($id(this.node));
	var self = this;
	this.dd.onDrag = function(_12e) {
		self.notifyMove();
	};
	this.dd.addInvalidHandleType("INPUT");
	this.dd.addInvalidHandleType("SELECT");
	this.dd.addInvalidHandleClass("terminal");
	this.dd.addInvalidHandleClass("wire");
	this.container = null;
};
maxwell.Block.prototype.fireOnMove = function() {
	for ( var i = 0; i < this.terminals.length; i++) {
		this.terminals[i].fireOnMove();
	}
	this.onmove.fire(this.XY);
};
maxwell.Block.prototype.setXY = function(_130) {
	YAHOO.util.Dom.setXY(this.node, _130);
	this.XY = _130;
	for ( var i = 0; i < this.terminals.length; i++) {
		this.terminals[i].onmove.fire($centerXY(this.terminals[i].node));
	}
	this.fireOnMove();
};
maxwell.Block.prototype.updateXY = function() {
	this.XY = YAHOO.util.Dom.getXY(this.node);
};
maxwell.Block.prototype.getXY = function() {
	if (!this.XY) {
		this.updateXY();
	}
	return this.XY;
};
maxwell.Block.prototype.addTerminal = function(t) {
	this.terminals.push(t);
	t.block = this;
	this.node.appendChild(t.node);
	this.onnewterminal.fire(t);
	var self = this;
	t.onremove.subscribe(function(type, args, me) {
		for ( var i = 0; i < self.terminals.length; i++) {
			if (self.terminals[i] == t) {
				self.terminals.splice(i, 1);
				self.onremoveterminal.fire(t);
			}
		}
	}, this, true);
};
maxwell.Block.prototype.remove = function() {
	if (this.removed) {
		return;
	}
	this.removed = true;
	for ( var i = this.terminals.length - 1; i >= 0; i--) {
		this.terminals[i].remove();
	}
	if (this.node.parentNode) {
		this.node.parentNode.removeChild(this.node);
	}
	this.onremove.fire();
};
$namespace("maxwell.Terminal");
maxwell.Terminal = function(_139) {
	if (!_139) {
		this.config = maxwell.Wire.Config;
	} else {
		this.config = _139;
	}
	if (!_139.placement) {
		_139.placement = "";
	}
	var _13a = "terminal " + _139.placement;
	this.id = _139.id ? _139.id : maxwell.Util.newId("terminal");
	this.wires = [];
	this.config = _139;
	this.onmove = new YAHOO.util.CustomEvent("onmove", this);
	this.onremove = new YAHOO.util.CustomEvent("onremove", this);
	this.onnewwire = new YAHOO.util.CustomEvent("onnewwire", this);
	this.onbeforenewwire = new YAHOO.util.CustomEvent("onbeforenewwire", this);
	this.onremovewire = new YAHOO.util.CustomEvent("onremovewire", this);
	this.onstylechange = new YAHOO.util.CustomEvent("onstylechange", this);
	this.node = cn("div", {
		className : _13a
	}, null);
	this.render = cn("div", {
		className : "terminalrender"
	}, null);
	this.node.appendChild(this.render);
	if (_139.placement == "inside") {
		this.render.appendChild(cn("img", {
			className : "renderimage",
			src : "http://l.yimg.com/a/i/space.gif",
			width : "14px",
			height : "14px"
		}, {
			width : "14px",
			height : "14px"
		}));
	}
	this.direction = [];
	if (YAHOO.util.Dom.hasClass(this.node, "inside")) {
		this.direction[0] = 1;
		this.direction[1] = -0.33;
	} else {
		this.direction[1] = YAHOO.util.Dom.hasClass(this.node, "north") ? -1
				: YAHOO.util.Dom.hasClass(this.node, "south") ? 1 : 0;
		this.direction[0] = YAHOO.util.Dom.hasClass(this.node, "east") ? 1
				: YAHOO.util.Dom.hasClass(this.node, "west") ? -1 : 0;
	}
};
maxwell.Terminal.CSSRegex = /^.*_(\d)(.{6})(.{6})_/;
maxwell.Terminal.prototype.getRenderStyle = function() {
	if (this._renderStyle) {
		return this._renderStyle;
	}
	this._renderStyle = {};
	var bg = YAHOO.util.Dom.getStyle(this.render, "backgroundImage");
	var _13c = maxwell.Terminal.CSSRegex.exec(bg);
	if (_13c && _13c.length > 0) {
		this._renderStyle.color = "#" + _13c[2];
		this._renderStyle.width = 3;
		this._renderStyle.bordercolor = "#" + _13c[3];
		this._renderStyle.borderwidth = _13c[1] - 1;
	} else {
		this._renderStyle.color = "#fbffcc";
		this._renderStyle.width = 3;
		this._renderStyle.bordercolor = "#abaf8c";
		this._renderStyle.borderwidth = 1;
	}
	this._renderStyle.cap = "round";
	this._renderStyle.bordercap = "round";
	return this._renderStyle;
};
maxwell.Terminal.prototype.addWire = function(wire) {
	this.onbeforenewwire.fire(wire);
	this.wires.push(wire);
	this.onnewwire.fire(wire);
	YAHOO.util.Dom.addClass(this.render, "connected");
	this._renderStyle = null;
	this.getRenderStyle();
	this.onstylechange.fire(this);
	var self = this;
	wire.onremove.subscribe(function(type, args, me) {
		for ( var i = 0; i < self.wires.length; i++) {
			if (self.wires[i] == wire) {
				self.wires.splice(i, 1);
				self.onremovewire.fire(wire);
				break;
			}
		}
		if (self.wires.length == 0) {
			YAHOO.util.Dom.removeClass(self.render, "connected");
			self._renderStyle = null;
			self.getRenderStyle();
			self.onstylechange.fire(self);
		}
	}, this, true);
};
maxwell.Terminal.prototype.remove = function() {
	if (this.removed) {
		return;
	}
	this.removed = true;
	if (!this.node) {
		return;
	}
	for ( var i = this.wires.length - 1; i >= 0; i--) {
		this.wires[i].remove();
	}
	this.onremove.fire();
	if (this.node.parentNode) {
		this.node.parentNode.removeChild(this.node);
	}
};
$namespace("maxwell.IOTerminal");
maxwell.IOTerminal = function(_144) {
	this.onpreviewavailable = new YAHOO.util.CustomEvent("onpreviewavailable",
			this, true);
	this.onfieldpreviewstart = new YAHOO.util.CustomEvent(
			"onfieldpreviewstart", this, true);
	this.onfieldpreviewend = new YAHOO.util.CustomEvent("onfieldpreviewend",
			this, true);
	this.onfieldpreviewavailable = new YAHOO.util.CustomEvent(
			"onfieldpreviewavailable", this, true);
	this.ontypechange = new YAHOO.util.CustomEvent("ontypechange", this);
	if (_144.dynamicType) {
		_144.types = [];
		this.onfieldpreviewavailable.subscribe(this.changeType, this, true);
	}
	maxwell.IOTerminal.superclass.constructor.call(this, _144);
	this.inside = (this.config.placement == "inside");
	this.initDragDrop();
};
$extend(maxwell.IOTerminal, maxwell.Terminal);
maxwell.IOTerminal.prototype.setPreviewData = function(data) {
	this.previewData = data;
	this.onpreviewavailable.fire(data);
};
maxwell.IOTerminal.prototype.startFieldPreview = function(_146) {
	this.onfieldpreviewstart.fire(_146);
};
maxwell.IOTerminal.prototype.endFieldPreview = function(_147) {
	this.onfieldpreviewend.fire(_147);
};
maxwell.IOTerminal.prototype.setFieldPreviewData = function(_148, data) {
	this.previewData = data;
	this.onfieldpreviewavailable.fire(_148, data);
};
maxwell.IOTerminal.prototype.changeType = function(type, args, self) {
	for ( var key in this.config.types) {
		YAHOO.util.Dom.removeClass(this.render, this.config.types[key]);
		YAHOO.util.Dom.removeClass(this.node, this.config.types[key]
				+ this.config.placement);
	}
	var data = args[1];
	var type = null;
	if (!data || !data._type) {
		this.config.types = [];
	} else {
		type = data._type;
		this.config.types = [ type ];
	}
	this._renderStyle = null;
	for ( var key in this.config.types) {
		YAHOO.util.Dom.addClass(this.render, this.config.types[key]);
		YAHOO.util.Dom.addClass(this.node, this.config.types[key]
				+ this.config.placement);
	}
	this.initDragDrop();
	for ( var i = this.wires.length - 1; i >= 0; i--) {
		var w = this.wires[0];
		var tgt = w.getTarget();
		var _152 = tgt.types[0];
		if (!type || !maxwell.HtmlPlus.InputType.CanCast(type, _152)) {
			w.remove();
		}
	}
	this.ontypechange.fire(type);
};
maxwell.IOTerminal.prototype.setCSS = function(_153, _154, _155) {
	YAHOO.util.Dom.addClass(this.render, _154);
	for ( var key in this.config.types) {
		YAHOO.util.Dom.addClass(this.render, this.config.types[key]);
		YAHOO.util.Dom.addClass(this.node, this.config.types[key]
				+ this.config.placement);
	}
};
maxwell.IOTerminal.prototype.setXY = function(xy) {
	this.node.style.left = xy[0] + "px";
	this.node.style.top = xy[1] + "px";
	this.updateXY();
};
maxwell.IOTerminal.prototype.updateXY = function() {
	var x = 0;
	var y = 0;
	if (this.inside) {
		if (!this.editcontainer) {
			this.editcontainer = this.node;
			while (!YAHOO.util.Dom
					.hasClass(this.editcontainer, "editcontainer")) {
				this.editcontainer = this.editcontainer.offsetParent;
			}
			this.row = this.render.offsetWidth / 2;
			this.roh = this.render.offsetHeight / 2;
		}
		var n = this.node;
		while (n != this.editcontainer) {
			x += n.offsetLeft;
			y += n.offsetTop;
			n = n.offsetParent;
		}
		x += this.row;
		y += this.roh;
	} else {
		if (!this.op) {
			this.op = this.node.offsetParent;
		}
		x = this.op.offsetLeft + this.node.offsetLeft;
		y = this.op.offsetTop + this.node.offsetTop;
	}
	this.XY = [ x, y ];
};
maxwell.IOTerminal.prototype.fireOnMove = function(_15b, _15c) {
	if (this.wires.length == 0) {
		return;
	}
	if (_15c) {
		this.XY[0] += _15c[0];
		this.XY[1] += _15c[1];
	} else {
		this.updateXY();
		this.wires[0].dirty = true;
		if (this.wires[0].src != this) {
			this.wires[0].src.updateXY();
		}
		if (this.wires[0].tgt != this) {
			this.wires[0].tgt.updateXY();
		}
	}
	this.onmove.fire(this.XY);
};
maxwell.IOTerminal.prototype.getXY = function() {
	if (!this.XY) {
		this.updateXY();
	}
	return this.XY;
};
maxwell.IOTerminal.prototype.initDragDrop = function() {
	if (this.dd) {
		this.dd.unreg();
	}
	if (this.dds) {
		this.dds.unreg();
	}
	this.config.acceptTypes = [];
	this.config.emitTypes = [];
	for ( var i = 0; i < this.config.types.length; i++) {
		this.config.acceptTypes.push("accept-" + this.config.types[i]);
		this.config.emitTypes.push("emit-" + this.config.types[i]);
	}
	if (this.config.type != "input") {
		var t = this.config.acceptTypes;
		this.config.acceptTypes = this.config.emitTypes;
		this.config.emitTypes = t;
	}
	if (this.config.placement == "inside") {
		this.dd = new maxwell.DragDrop.Target($id(this.node),
				this.config.acceptTypes);
	} else {
		this.dd = new maxwell.DragDrop.Target($id(this.render),
				this.config.acceptTypes);
	}
	this.dd.startSourceDrag.subscribe(function() {
		YAHOO.util.Dom.addClass(this.render, "target");
	}, this, true);
	this.dd.endSourceDrag.subscribe(function() {
		YAHOO.util.Dom.removeClass(this.render, "over");
		YAHOO.util.Dom.removeClass(this.render, "target");
	}, this, true);
	this.dd.onSourceDragEnter.subscribe(function() {
		YAHOO.util.Dom.addClass(this.render, "over");
	}, this, true);
	this.dd.onSourceDragOut.subscribe(function() {
		YAHOO.util.Dom.removeClass(this.render, "over");
	}, this, true);
	this.dd.onSourceDragDrop.subscribe(function(type, args, me) {
		var _162 = args[0].getData();
		if ((this.wires.length > 0 && this.config.singleWire)
				|| _162.block == this.block) {
			return;
		}
		YAHOO.util.Dom.removeClass(this.render, "over");
		YAHOO.util.Dom.removeClass(this.render, "target");
		if (this.config.type == "input") {
			new maxwell.Wire(_162, this);
		} else {
			new maxwell.Wire(this, _162);
		}
	}, this, true);
	var self = this;
	this.dd.getData = function() {
		return self;
	};
	this.dds = new maxwell.DragDrop.Source($id(this.render), this,
			this.config.emitTypes);
	this.dds.onStartDrag.subscribe(this.startDrag, this, true);
	this.dds.onEndDrag.subscribe(this.endDrag, this, true);
	this.dds.onDragging.subscribe(this.onDrag, this, true);
	this.dds.onAcquiredTargets.subscribe(this.modifyTargets, this, true);
};
maxwell.IOTerminal.prototype.modifyTargets = function(type, args, me) {
	var _167 = args[1];
	if (this.config.singleWire) {
		if (this.wires.length > 0) {
			this.wires[0].remove();
		}
	}
	for ( var key in _167) {
		var t = _167[key].getData();
		if (t.block == this.block
				|| (t.config.singleWire && t.wires.length > 0)) {
			_167[key] = null;
		}
	}
};
maxwell.IOTerminal.prototype.startDrag = function(type, args, me) {
	var _16d = [];
	for ( var k in this.dds.dataTypes) {
		var g = "datadd-" + this.dds.dataTypes[k];
		_16d[g] = g;
	}
	YAHOO.util.DragDropMgr.refreshCache(_16d);
	var n = this.node.offsetParent;
	while (!YAHOO.util.Dom.hasClass(n, "editcontainer")) {
		n = n.offsetParent;
	}
	this.origin = YAHOO.util.Dom.getXY(n);
	if (n.scrollTop) {
		this.origin[1] -= n.scrollTop;
	}
	if (n.scrollLeft) {
		this.origin[0] -= n.scrollLeft;
	}
	this.origin[1] -= this.render.offsetHeight / 2;
	this.origin[0] -= this.render.offsetWidth / 2;
	this.updateXY();
	var el = this.dds.getDragEl();
	el.node = el;
	el.direction = [ 0, 0 ];
	el.onmove = new YAHOO.util.CustomEvent("onmove", el);
	var self = this;
	el.getXY = function() {
		return self.getElXY(el);
	};
	this.ddwire = new maxwell.Wire(this, el, {
		className : "dragging"
	});
};
maxwell.IOTerminal.prototype.endDrag = function(_173) {
	this.ddwire.remove();
	var _174 = [];
	for ( var k in this.dds.dataTypes) {
		var g = "datadd-" + this.dds.dataTypes[k];
		_174[g] = g;
	}
	YAHOO.util.DragDropMgr.refreshCache(_174);
};
maxwell.IOTerminal.prototype.onDrag = function(_177) {
	var el = this.dds.getDragEl();
	el.onmove.fire(this.getElXY(el));
};
maxwell.IOTerminal.prototype.remove = function() {
	this.dd.unreg();
	this.dds.unreg();
	maxwell.IOTerminal.superclass.remove.call(this);
};
maxwell.IOTerminal.prototype.getElXY = function(el) {
	var p = [ el.offsetLeft, el.offsetTop ];
	p[0] -= this.origin[0];
	p[1] -= this.origin[1];
	return p;
};
$namespace("maxwell.InputTerminal");
maxwell.InputTerminal = function(_17b) {
	_17b.type = "input";
	_17b.types = maxwell.HtmlPlus.Util.GetCastables(_17b.types[0]);
	this.types = _17b.types;
	_17b.singleWire = true;
	maxwell.InputTerminal.superclass.constructor.call(this, _17b);
	this.setCSS(_17b.types, "input", "tt-");
	this.onnewwire.subscribe(
			function(type, args, me) {
				var src = args[0].getSource();
				src.onpreviewavailable
						.subscribe(this.refirePreview, this, true);
				src.onfieldpreviewstart.subscribe(this.refireFieldPreviewStart,
						this, true);
				src.onfieldpreviewend.subscribe(this.refireFieldPreviewEnd,
						this, true);
				src.onfieldpreviewavailable.subscribe(function(type, args, me) {
					this.refireFieldPreviewAvailable(type, args, me);
				}, this, true);
				if (src.previewData) {
					this.setPreviewData(src.previewData);
				}
			}, this, true);
	this.onremovewire.subscribe(function(type, args, me) {
		var src = args[0].getSource();
		src.onpreviewavailable.unsubscribe(this.refirePreview, this, true);
		src.onfieldpreviewstart.unsubscribe(this.refireFieldPreviewStart, this,
				true);
		src.onfieldpreviewend.unsubscribe(this.refireFieldPreviewEnd, this,
				true);
		src.onfieldpreviewavailable.unsubscribe(
				this.refireFieldPreviewAvailable, this, true);
	}, this, true);
};
$extend(maxwell.InputTerminal, maxwell.IOTerminal);
maxwell.InputTerminal.prototype.refirePreview = function(type, args, me) {
	this.setPreviewData(args[0]);
};
maxwell.InputTerminal.prototype.refireFieldPreviewStart = function(type, args,
		me) {
	this.startFieldPreview(args[0]);
};
maxwell.InputTerminal.prototype.refireFieldPreviewAvailable = function(type,
		args, me) {
	this.setFieldPreviewData(args[0], args[1], args[2]);
};
maxwell.InputTerminal.prototype.refireFieldPreviewEnd = function(type, args, me) {
	this.endFieldPreview(args[0]);
};
$namespace("maxwell.OutputTerminal");
maxwell.OutputTerminal = function(_193) {
	_193.type = "output";
	this.types = _193.types;
	maxwell.OutputTerminal.superclass.constructor.call(this, _193);
	this.setCSS(_193.types, "output", "tt-");
};
$extend(maxwell.OutputTerminal, maxwell.IOTerminal);
$namespace("maxwell.WireBlockContainer");
maxwell.WireBlockContainer = function(node) {
	this.node = node;
	this.blocks = [];
	this.wires = [];
	this.invitearea = cn("div", {
		className : "dropareainvite"
	}, null, "<div class='droptext'>drag modules here</div>");
	this.node.appendChild(this.invitearea);
	this.onnewblock = new YAHOO.util.CustomEvent("onnewblock", this);
	this.onremoveblock = new YAHOO.util.CustomEvent("onremoveblock", this);
	this.clearing = false;
};
maxwell.WireBlockContainer.prototype.addBlock = function(_195, xy) {
	if (this.invitearea != null && !this.fading) {
		this.fading = true;
		var anim = new YAHOO.util.Anim(this.invitearea, {
			opacity : {
				to : 0
			}
		}, 1, YAHOO.util.Easing.easeOut);
		var self = this;
		anim.onComplete.subscribe(function() {
			self.node.removeChild(self.invitearea);
			self.invitearea = null;
		});
		anim.animate();
	}
	this.node.appendChild(_195.node);
	if (xy) {
		_195.node.style.left = xy[0] + "px";
		_195.node.style.top = xy[1] + "px";
	}
	this.blocks[this.blocks.length] = _195;
	_195.container = this;
	var self = this;
	_195.onremove.subscribe(function(type, args, me) {
		for ( var i = 0; i < self.blocks.length; i++) {
			if (self.blocks[i] == _195) {
				self.blocks.splice(i, 1);
				self.onremoveblock.fire(_195);
			}
		}
	}, true);
	this.onnewblock.fire(_195);
};
maxwell.WireBlockContainer.prototype.layout = function() {
};
maxwell.WireBlockContainer.prototype.clear = function() {
	this.clearing = true;
	for ( var i = this.blocks.length - 1; i >= 0; i--) {
		this.blocks[i].remove();
	}
	this.clearing = false;
};
$namespace("maxwell.HierarchyLayoutContainer");
maxwell.HierarchyLayoutContainer = function(node) {
	maxwell.HierarchyLayoutContainer.superclass.constructor.call(this, node);
};
$extend(maxwell.HierarchyLayoutContainer, maxwell.WireBlockContainer);
maxwell.HierarchyLayoutContainer.prototype.layout = function(_19f) {
	var _1a0 = [];
	var _1a1 = [];
	var _1a2 = [];
	maxwell.HtmlPlus.Util.CloseInputs();
	if (!_19f) {
		_19f = [ 0, 0 ];
	}
	for ( var i = 0; i < this.blocks.length; i++) {
		var b = this.blocks[i];
		if (_1a2[b.id] || _1a1[b.id]) {
			continue;
		}
		var _1a5 = [];
		var _1a6 = [];
		this.walkForward(b, _1a2, _1a5);
		for ( var j = 0; j < _1a5.length; j++) {
			this.walkBack(_1a5[j], _1a1, 0, _1a6);
		}
		_1a0.push(_1a6);
	}
	var _1a8 = 0;
	var _1a9 = 20;
	var _1aa = 50;
	var _1ab = [];
	var _1ac = 0;
	for ( var i = 0; i < _1a0.length; i++) {
		var _1ad = 0;
		for ( var j = 0; j < _1a0[i].length; j++) {
			var _1ae = 0;
			var _1af = 0;
			for ( var k = 0; k < _1a0[i][j].length; k++) {
				var _1b1 = YAHOO.util.Dom.getRegion(_1a0[i][j][k].node);
				var _1b2 = _1b1.bottom - _1b1.top;
				var _1b3 = _1b1.right - _1b1.left;
				_1a0[i][j][k].nodeWidth = _1b3;
				_1a0[i][j][k].nodeHeight = _1b2;
				_1ae = _1ae + _1b3 + _1a9;
				if (_1b2 > _1af) {
					_1af = _1b2;
				}
			}
			_1a0[i][j].width = _1ae;
			_1a0[i][j].height = _1af;
			if (!_1ab[j]) {
				_1ab[j] = 0;
			}
			if (_1ab[j] < _1af) {
				_1ab[j] = _1af;
			}
			if (_1ae > _1ad) {
				_1ad = _1ae;
			}
		}
		_1a0[i].width = _1ad;
		_1ac += _1ad;
		if (_1a0[i].length > _1a8) {
			_1a8 = _1a0[i].length;
		}
	}
	var _1b4 = _19f[0];
	for ( var i = 0; i < _1a0.length; i++) {
		var _1b5 = _19f[1];
		for ( var j = _1a0[i].length - 1; j >= 0; j--) {
			var x = _1b4
					+ (_1a0[i].width * (_1a0[i].width - _1a0[i][j].width) / (_1a0[i].width * 2));
			var y = _1b5;
			for ( var k = 0; k < _1a0[i][j].length; k++) {
				var yy = y;
				_1a0[i][j][k].setXY([ x, yy ]);
				x += _1a0[i][j][k].nodeWidth + _1a9;
			}
			_1b5 += _1ab[j] + _1aa;
		}
		_1b4 += _1a0[i].width;
	}
};
maxwell.HierarchyLayoutContainer.prototype.walkBack = function(b, _1ba, _1bb,
		_1bc) {
	if (!_1bc[_1bb]) {
		_1bc[_1bb] = [];
	}
	if (_1ba[b.id]) {
		for ( var i = 0; i < _1bc.length; i++) {
			for ( var j = 0; j < _1bc[i].length; j++) {
				if (_1bc[i][j] == b) {
					if (i >= _1bb) {
						return;
					}
					_1bc[i].splice(j, 1);
					break;
				}
			}
		}
	}
	_1ba[b.id] = true;
	_1bc[_1bb].push(b);
	var usbs = this.getUpstreamBlocks(b);
	for ( var i = 0; i < usbs.length; i++) {
		this.walkBack(usbs[i], _1ba, _1bb + 1, _1bc);
	}
};
maxwell.HierarchyLayoutContainer.prototype.walkForward = function(b, _1c1, _1c2) {
	if (_1c1[b.id]) {
		return;
	}
	_1c1[b.id] = true;
	var dsbs = this.getDownstreamBlocks(b);
	if (dsbs.length == 0) {
		_1c2.push(b);
	}
	for ( var i = 0; i < dsbs.length; i++) {
		this.walkForward(dsbs[i], _1c1, _1c2);
	}
};
maxwell.HierarchyLayoutContainer.prototype.getUpstreamBlocks = function(_1c5) {
	var _1c6 = [];
	for ( var i = 0; i < _1c5.terminals.length; i++) {
		var t = _1c5.terminals[i];
		for ( var j = 0; j < t.wires.length; j++) {
			var w = t.wires[j];
			var b2 = this.wireInputBlock(w);
			if (b2.id == _1c5.id) {
				continue;
			}
			_1c6.push(b2);
		}
	}
	var _1cc = _1c5.submodules;
	for ( var j = 0; j < _1c5.submodules.length; j++) {
		_1c6 = _1c6.concat(this.getUpstreamBlocks(_1c5.submodules[j]));
	}
	return _1c6;
};
maxwell.HierarchyLayoutContainer.prototype.getDownstreamBlocks = function(_1cd) {
	var _1ce = [];
	for ( var i = 0; i < _1cd.terminals.length; i++) {
		var t = _1cd.terminals[i];
		for ( var j = 0; j < t.wires.length; j++) {
			var w = t.wires[j];
			var b2 = this.wireOutputBlock(w);
			if (_1cd.id == b2.id) {
				continue;
			}
			_1ce.push(b2);
		}
	}
	return _1ce;
};
maxwell.HierarchyLayoutContainer.prototype.getTrueBlock = function(_1d4) {
	while (_1d4) {
		for ( var i = 0; i < this.blocks.length; i++) {
			var b = this.blocks[i];
			if (_1d4.id == b.id) {
				return _1d4;
			}
		}
		_1d4 = maxwell.Module.getModule(_1d4.node.parentNode);
	}
	return null;
};
maxwell.HierarchyLayoutContainer.prototype._wireOutputBlock = function(wire) {
	if (wire.tgt.config["type"] == "input") {
		return wire.tgt.block;
	}
	return wire.src.block;
};
maxwell.HierarchyLayoutContainer.prototype._wireInputBlock = function(wire) {
	if (wire.tgt.config["type"] == "output") {
		return wire.tgt.block;
	}
	return wire.src.block;
};
maxwell.HierarchyLayoutContainer.prototype.wireOutputBlock = function(wire) {
	return this.getTrueBlock(this._wireOutputBlock(wire));
};
maxwell.HierarchyLayoutContainer.prototype.wireInputBlock = function(wire) {
	return this.getTrueBlock(this._wireInputBlock(wire));
};
$namespace("maxwell.Editor");
maxwell.Editor = function() {
	var node = cn("div", {
		className : "editor"
	});
	this.node = node;
	this.id = maxwell.Util.newId("editor");
	var _1dc = cn("div", {
		className : "editcontainer"
	});
	this.onsave = new YAHOO.util.CustomEvent("onsave", this, true);
	this.onload = new YAHOO.util.CustomEvent("onload", this, true);
	this.onclone = new YAHOO.util.CustomEvent("onclone", this, true);
	this.onsavestart = new YAHOO.util.CustomEvent("onsavestart", this, true);
	this.onloadstart = new YAHOO.util.CustomEvent("onloadstart", this, true);
	this.onclonestart = new YAHOO.util.CustomEvent("onclonestart", this, true);
	this.onpreview = new YAHOO.util.CustomEvent("onpreview", this, true);
	this.onpreviewerror = new YAHOO.util.CustomEvent("onpreviewerror", this,
			true);
	this.onchange = new YAHOO.util.CustomEvent("onchange", this, true);
	this.onremove = new YAHOO.util.CustomEvent("onremove", this, true);
	this.layout = new maxwell.HierarchyLayoutContainer(_1dc);
	this.rssdebugger = new maxwell.RSSDebugger(this);
	this.pipe = {
		is_owner : true,
		name : null,
		desc : null,
		id : null,
		publish : false
	};
	this.undostate = [];
	this.undolevel = 0;
	this.undoing = false;
	this.dirty = false;
	this.restoring = false;
	this.saving = false;
	this.cloning = false;
	this.canSave = true;
	this.layout.onnewblock.subscribe(this.onnewblock, this, true);
	this.layout.onremoveblock.subscribe(this.onremoveblock, this, true);
	node.appendChild(_1dc);
	node.appendChild(this.rssdebugger.node);
	this.splitter = new maxwell.Splitter.TopBottom(_1dc, this.rssdebugger.node);
	this.rssdebugger.setSplitter(this.splitter);
	this.initlayout();
	this.lastChange = new Date();
};
maxwell.Editor.prototype.initlayout = function() {
	this.layoutTarget = new maxwell.DragDrop.Target($id(this.layout.node), [
			"moduletype", "rssmodule" ]);
	this.layoutTarget.onSourceDragDrop.subscribe(function(type, args, me) {
		var _1e0 = args[0];
		var _1e1 = null;
		var name = _1e0.data;
		if (_1e0.hasDataType("moduletype")) {
			var _1e3 = args[0].getData();
			_1e1 = maxwell.Module.Create(_1e3, {
				name : name
			});
		} else {
			var _1e4 = _1e0.getData();
			if (_1e4.source == "pipe") {
				_1e1 = maxwell.Module.Create(_1e4.type, {
					name : name
				});
			} else {
				var conf = {
					"URL" : {
						type : "url",
						value : _1e4.url
					}
				};
				_1e1 = maxwell.Module.Create("fetch", {
					conf : conf,
					name : name
				});
			}
		}
		if (_1e1) {
			var xy = args[1];
			var r = YAHOO.util.Dom.getRegion(this.layout.node);
			if (this.layout.node.scrollTop) {
				xy[1] += this.layout.node.scrollTop;
			}
			if (this.layout.node.scrollLeft) {
				xy[0] += this.layout.node.scrollLeft;
			}
			xy[0] -= r.left;
			xy[1] -= r.top;
			xy[0] -= 80;
			xy[1] -= 20;
			this.addModule(_1e1, args[1]);
		}
	}, this, true);
};
maxwell.Editor.prototype.getModuleByType = function(type) {
	for ( var i = 0; i < this.layout.blocks.length; i++) {
		if (this.layout.blocks[i].type == type) {
			return this.layout.blocks[i];
		}
	}
	return null;
};
maxwell.Editor.prototype.getModuleById = function(id) {
	for ( var i = 0; i < this.layout.blocks.length; i++) {
		if (this.layout.blocks[i].id == id) {
			return this.layout.blocks[i];
		}
		var m = this.layout.blocks[i].getModuleById(id);
		if (m) {
			return m;
		}
	}
	return null;
};
maxwell.Editor.prototype.getTerminalById = function(id) {
	for ( var i = 0; i < this.layout.blocks.length; i++) {
		var t = this.layout.blocks[i].getTerminalById(id);
		if (t) {
			return t;
		}
	}
	return null;
};
maxwell.Editor.prototype.hasModule = function(_1f0) {
	for ( var i = 0; i < this.layout.blocks.length; i++) {
		if (this.layout.blocks[i] == _1f0) {
			return true;
		}
		var m = this.layout.blocks[i].hasModule(_1f0);
		if (m) {
			return m;
		}
	}
	return false;
};
maxwell.Editor.prototype.collapseAll = function() {
	for ( var i = 0; i < this.layout.blocks.length; i++) {
		this.layout.blocks[i].hidebd();
	}
};
maxwell.Editor.prototype.expandAll = function() {
	for ( var i = 0; i < this.layout.blocks.length; i++) {
		this.layout.blocks[i].showbd();
	}
};
maxwell.Editor.prototype.preview = function(_1f5) {
	if (this.runningFieldPreview
			&& YAHOO.util.Connect.isCallInProgress(this.runningFieldPreview)) {
		ajaxStop(this.runningFieldPreview);
		this.runningFieldpreview = null;
	}
	if (this.runningPreview
			&& YAHOO.util.Connect.isCallInProgress(this.runningPreview)) {
		if (this.runningPreview.startTime < this.lastChange) {
			ajaxStop(this.runningPreview);
			this.runningPreview = null;
		} else {
			return;
		}
	}
	maxwell.PipesEditor.ClearStatus();
	for ( var i = 0; i < this.layout.blocks.length; i++) {
		this.layout.blocks[i].previewstart();
	}
	var self = this;
	var vars = {
		_out : "json"
	};
	if (_1f5) {
		vars["end"] = _1f5;
	}
	var _1f9 = this.freeze();
	_1f9 = {
		modules : _1f9.modules,
		wires : _1f9.wires
	};
	if (isV2) {
		vars["_def"] = $JSON(_1f9);
	} else {
		vars["def"] = $JSON(_1f9);
	}
	vars["rss"] = 1;
	this.runningPreview = ajaxCall("pipe/preview", vars, function(data) {
		if (data) {
			var _1fb = data.errors;
			data = data.preview;
			for ( var i = 0; i < self.layout.blocks.length; i++) {
				var _1fd = self.layout.blocks[i];
				var _1fe = data[_1fd.id];
				var _1ff = _1fb.modules[_1fd.id];
				if (_1fe) {
					_1fd.preview(_1fe, _1ff);
				} else {
					_1fd.preview(null, _1ff);
				}
			}
			self.previewData = data;
			self.errorData = _1fb;
			self.onpreview.fire(data);
		} else {
			for ( var i = 0; i < self.layout.blocks.length; i++) {
				self.layout.blocks[i].preview(null, null);
			}
			self.onpreviewerror.fire("No preview content");
		}
	}, function(_200) {
		for ( var i = 0; i < self.layout.blocks.length; i++) {
			self.layout.blocks[i].preview(null, null);
		}
		self.onpreviewerror.fire(_200);
		if (_200 != "transaction aborted") {
			maxwell.PipesEditor.SetStatus(_200, "Preview Failed");
		}
	}, "POST");
	this.runningPreview.startTime = new Date(this.lastChange.valueOf());
};
maxwell.Editor.prototype.fieldpreview = function(_202) {
	if (this.restoring) {
		return;
	}
	if (this.runningFieldPreview
			&& YAHOO.util.Connect.isCallInProgress(this.runningFieldPreview)) {
		if (this.runningFieldPreview.startTime < this.lastChange) {
			ajaxStop(this.runningFieldPreview);
			this.runningFieldPreview = null;
		} else {
			return;
		}
	}
	if (this.runningPreview
			&& YAHOO.util.Connect.isCallInProgress(this.runningPreview)) {
		if (this.runningPreview.startTime < this.lastChange) {
			ajaxStop(this.runningPreview);
			this.runningPreview = null;
		} else {
			return;
		}
	}
	maxwell.PipesEditor.ClearStatus();
	var _203 = function(_204, _205) {
		var _206 = [];
		if (!_205) {
			_205 = {};
		}
		if (_205[_204.id]) {
			return _206;
		}
		_205[_204.id] = true;
		for ( var i = 0; i < _204.terminals.length; i++) {
			var t = _204.terminals[i];
			_206.push(t);
			for ( var j = 0; j < t.wires.length; j++) {
				var w = t.wires[j];
				var m = w.getTarget().block;
				if (m == _204 || _205[m.id]) {
					continue;
				}
				_206 = _206.concat(_203(m, _205));
			}
		}
		return _206;
	};
	var _20c = [];
	if (!_202) {
		var _20d = {};
		for ( var i = 0; i < this.layout.blocks.length; i++) {
			_20c = _20c.concat(_203(this.layout.blocks[i], _20d));
		}
	} else {
		_20c = _203(_202);
	}
	var vars = {
		_out : "json"
	};
	if (_202) {
	}
	var _210 = this.freeze();
	_210 = {
		modules : _210.modules,
		wires : _210.wires
	};
	if (isV2) {
		vars["_def"] = $JSON(_210);
	} else {
		vars["def"] = $JSON(_210);
	}
	vars["rss"] = 0;
	var _211 = "preview" + Math.round(Math.random() * 100000);
	for ( var i = 0; i < _20c.length; i++) {
		_20c[i].startFieldPreview(_211);
	}
	var self = this;
	this.runningFieldPreview = ajaxCall("pipe/preview", vars, function(data) {
		if (data && data.preview) {
			data = data.preview;
			for ( var id in data) {
				var _215 = self.getModuleById(id);
				if (!_215) {
					continue;
				}
				var _216 = data[id];
				if (!_216.prop) {
					continue;
				}
				for ( var tid in _216.prop) {
					var t = _215.getTerminalById(tid);
					if (!t) {
						continue;
					}
					var prop = _216.prop[tid];
					t.setFieldPreviewData(_211, prop);
					if (tid == "_OUTPUT" && _215.type == "split") {
						t = _215.getTerminalById("_OUTPUT2");
						if (t) {
							t.setFieldPreviewData(_211, prop);
						}
					}
				}
			}
		}
		for ( var i = 0; i < _20c.length; i++) {
			_20c[i].endFieldPreview(_211);
		}
	}, function(_21b) {
		for ( var i = 0; i < _20c.length; i++) {
			_20c[i].endFieldPreview(_211);
		}
		if (_21b != "transaction aborted") {
			maxwell.PipesEditor.SetStatus(_21b, "Field Preview Failed");
		}
	}, "POST");
	this.runningFieldPreview.startTime = new Date(this.lastChange.valueOf());
};
maxwell.Editor.prototype.onnewwire = function(type, args, me) {
	var wire = args[0];
	var tgt = wire.getTarget();
	this.fieldpreview(tgt.block);
};
maxwell.Editor.prototype.onnewterminal = function(type, args, me) {
	var _225 = args[0];
	if (_225.id == "_OUTPUT") {
		return;
	}
	_225.onnewwire.subscribe(this.onnewwire, this, true);
};
maxwell.Editor.prototype.moduleselect = function(type, args, self) {
	var _229 = args[0];
	if (this.selectedmodule) {
		this.selectedmodule.unselect();
	}
	this.selectedmodule = _229;
	var _22a = function(_22b, _22c) {
		if (_22b.isPreviewStale()) {
			return true;
		}
		if (!_22c) {
			_22c = {};
		}
		if (_22c[_22b.id]) {
			return false;
		}
		_22c[_22b.id] = true;
		for ( var i = 0; i < _22b.terminals.length; i++) {
			var t = _22b.terminals[i];
			for ( var j = 0; j < t.wires.length; j++) {
				var w = t.wires[j];
				var m = w.getSource().block;
				if (_22a(m, _22c)) {
					return true;
				}
			}
		}
		return false;
	};
	var _232 = _22a(_229);
	this.rssdebugger.setSourceModule(_229, _232);
};
maxwell.Editor.prototype.onnewblock = function(type, args, me) {
	var _236 = args[0];
	_236.onselect.subscribe(this.moduleselect, this, true);
	_236.onnewterminal.subscribe(this.onnewterminal, this, true);
	for ( var i = 0; i < _236.terminals.length; i++) {
		this.onnewterminal(null, [ _236.terminals[i] ], this);
	}
	if (this.restoring) {
		return;
	}
	if (this.layout.blocks.length == 1) {
		this.verifyEndModule();
	}
	this.onChange(null, [ _236, null ]);
};
maxwell.Editor.prototype.onremoveblock = function(type, args, me) {
	var _23b = args[0];
	_23b.onnewterminal.unsubscribe(this.onnewterminal, this, true);
	_23b.onselect.unsubscribe(this.moduleselect, this, true);
	if (this.selectedmodule && this.selectedmodule == _23b) {
		this.selectedmodule = null;
	}
	for ( var i = 0; i < _23b.terminals.length; i++) {
		if (_23b.terminals[i].id == "_OUTPUT") {
			continue;
		}
		_23b.terminals[i].onnewwire.unsubscribe(this.onnewwire, this, true);
	}
	if (this.restoring) {
		return;
	}
	this.onChange(null, [ _23b, null ]);
};
maxwell.Editor.prototype.verifyEndModule = function() {
	for ( var i = 0; i < this.layout.blocks.length; i++) {
		if (this.layout.blocks[i].type == "output") {
			return;
		}
	}
	var _23e = new maxwell.Module({
		type : "output",
		id : "_OUTPUT"
	});
	this.layout.addBlock(_23e);
	var _23f = YAHOO.util.Dom.getRegion(this.layout.node);
	var _240 = YAHOO.util.Dom.getRegion(_23e.node);
	var xy = [ (_23f.left + _23f.right) / 2 - (_240.right - _240.left) / 2,
			_23f.bottom - (_240.bottom - _240.top) * 2 ];
	YAHOO.util.Dom.setXY(_23e.node, xy);
};
maxwell.Editor.prototype.load = function(_242) {
	if (_242) {
		this.onloadstart.fire(_242);
		var self = this;
		ajaxCall("pipe/load", {
			id : _242,
			_out : "json",
			modinfo : "true"
		}, function(data) {
			self.pipe = data.pipe;
			try {
				if (self.pipe.module_info) {
					for ( var ikey in self.pipe.module_info) {
						var info = self.pipe.module_info[ikey];
						maxwell.Module.setModuleInfo(ikey, info);
					}
				}
				var def = self.pipe.definition;
				self.restore(def);
			} catch (ex) {
				self.onload.fire(false, ex, self);
				return;
			}
		}, function(_248) {
			self.onload.fire(false, _248, self);
		});
	}
};
maxwell.Editor.prototype.clone = function() {
	if (!this.pipe) {
		throw "Not a saved pipe";
	}
	if (!this.pipe.id) {
		throw "Not a saved pipe";
	}
	if (this.cloning) {
		return;
	}
	this.cloning = true;
	this.onclonestart.fire(this.pipe.id);
	var self = this;
	ajaxCall("pipe/clone", {
		_out : "json",
		id : this.pipe.id
	}, function(data) {
		self.pipe.id = data.new_id;
		self.pipe.is_owner = true;
		if (self.dirty) {
			self.save(true);
		}
		self.onclone.fire(true, self.pipe, self);
		self.dirty = false;
		self.cloning = false;
	}, function(_24b) {
		self.onclone.fire(false, _24b, self);
		self.cloning = false;
	}, "POST");
};
maxwell.Editor.prototype.save = function(_24c) {
	if (this.saving) {
		return;
	}
	if (!this.pipe.name || this.pipe.name.length == 0) {
		var self = this;
		new maxwell.Dialog.Save({
			onOk : function(_24e) {
				self.pipe.name = _24e;
				self.save();
			}
		});
		return;
	}
	var vars = {
		_out : "json",
		name : this.pipe.name,
		desc : this.pipe.description,
		tags : this.pipe.tags,
		publish : this.pipe.publish
	};
	if (!_24c) {
		this.onsavestart.fire(vars["id"]);
	}
	if (this.pipe.id) {
		vars["id"] = this.pipe.id;
	}
	if (typeof (this.pipe.is_owner) != "undefined" && !this.pipe.is_owner) {
		this.dirty = true;
		this.clone();
		return;
	}
	if (isV2) {
		vars["_def"] = $JSON(this.freeze());
	} else {
		vars["def"] = $JSON(this.freeze());
	}
	this.saving = true;
	var self = this;
	ajaxCall("pipe/save", vars, function(data) {
		self.pipe.id = data.id;
		self.dirty = false;
		self.onsave.fire(true, self.pipe, self);
		self.saving = false;
	}, function(_251) {
		self.onsave.fire(false, _251, self);
		self.saving = false;
	}, "POST");
};
maxwell.Editor.prototype.debugLoadFromJson = function(json) {
	this.tmpJson = null;
	try {
		eval("this.tmpJson=" + json);
	} catch (ex) {
		return;
	}
	this.restore(this.tmpJson);
};
maxwell.Editor.prototype.cookieload = function() {
	var _253 = Cookie.get("rsspipe");
	if (_253) {
		this.pipe = _253.pipe;
		this.restore(_253.data);
		this.onload.fire(true, this.pipe);
	}
};
maxwell.Editor.prototype.cookiesave = function() {
	var _254 = {
		pipe : this.pipe,
		data : this.freeze()
	};
	Cookie.set("rsspipe", _254);
	this.dirty = false;
};
maxwell.Editor.prototype.onChange = function(type, args, self) {
	if (this.undoing || this.restoring) {
		return;
	}
	var _258 = null;
	var _259 = null;
	if (args && args.length > 0) {
		_258 = args[0];
		if (args.length > 1) {
			_259 = args[1];
		}
	}
	this.dirty = true;
	this.lastChange = new Date();
	this.onchange.fire(_258, _259, this);
	if (_259 && _259.tagName) {
		if (_259.getAttribute("previewonchange")) {
			this.fieldpreview(_258);
		}
	}
};
maxwell.Editor.prototype.undo = function() {
	if (this.undolevel == 0) {
		return false;
	}
	this.undolevel--;
	var _25a = this.undostate[this.undolevel - 1];
	this.undoing = true;
	this.pipe = _25a.pipe;
	this.restore(_25a.data);
	this.onload.fire(true, this.pipe);
	this.undoing = false;
	return true;
};
maxwell.Editor.prototype.redo = function() {
	if (this.undolevel == this.undostate.length) {
		return false;
	}
	var _25b = this.undostate[this.undolevel++];
	this.undoing = true;
	this.pipe = _25b.pipe;
	this.restore(_25b.data);
	this.onload.fire(true, this.pipe);
	this.undoing = true;
	return true;
};
maxwell.Editor.prototype.freeze = function() {
	var _25c = [];
	var _25d = [];
	var _25e = [];
	var _25f = [];
	for ( var i = 0; i < this.layout.blocks.length; i++) {
		var b = this.layout.blocks[i];
		var data = b.freeze();
		if (!data) {
			continue;
		}
		_25c.push(data);
		_25d.push({
			id : b.id,
			xy : YAHOO.util.Dom.getXY(b.node)
		});
	}
	var _263 = {};
	for ( var i = 0; i < this.layout.blocks.length; i++) {
		var b = this.layout.blocks[i];
		for ( var j = 0; j < b.terminals.length; j++) {
			var t = b.terminals[j];
			for ( var k = 0; k < t.wires.length; k++) {
				var w = t.wires[k];
				if (w.src.id != t.id) {
					continue;
				}
				var _268 = w.src.computedId;
				var _269 = w.tgt.computedId;
				_25e.push({
					id : w.id,
					src : {
						id : _268,
						moduleid : w.src.block.id
					},
					tgt : {
						id : _269,
						moduleid : w.tgt.block.id
					}
				});
				if (w.src.previewData && !_263[_268 + " " + w.src.block.id]) {
					_25f.push({
						id : _268,
						moduleid : w.src.block.id,
						data : w.src.previewData
					});
					_263[_268 + " " + w.src.block.id] = true;
				}
			}
		}
	}
	return {
		layout : _25d,
		modules : _25c,
		terminaldata : _25f,
		wires : _25e
	};
};
maxwell.Editor.prototype.restore = function(_26a) {
	var _26b = this.layout;
	_26b.clear();
	if (!_26a || !_26a.modules) {
		return;
	}
	this.restoring = true;
	var self = this;
	var _26d = 0;
	var mods = {};
	var _26f = {};
	var _270 = [ 10000, 10000 ];
	for ( var key in _26a.layout) {
		var p = _26a.layout[key];
		_26f[p.id] = p.xy;
		if (p.xy[0] < _270[0]) {
			_270[0] = p.xy[0];
		}
		if (p.xy[1] < _270[1]) {
			_270[1] = p.xy[1];
		}
	}
	_270[0] -= 25;
	_270[1] -= 25;
	var _273 = [];
	for ( var key in _26a.modules) {
		var _274 = _26a.modules[key];
		mods[_274.id] = maxwell.Module.Create(_274.type, {
			id : _274.id,
			conf : _274.conf
		});
		_26d++;
		mods[_274.id].module = _274;
		_273[_274.id] = _274.type;
		mods[_274.id].onready.subscribe(function() {
			_26d--;
			_273[this.id] = "";
		}, mods[_274.id], true);
		var p = _26f[_274.id];
		p[0] -= _270[0];
		p[1] -= _270[1];
		this.addModule(mods[_274.id], p);
	}
	var _275 = 0;
	var _276 = 11;
	var _277 = 0;
	var f = function() {
		if (_26d > 0) {
			if (_275 == _26d) {
				_277--;
			} else {
				_277 = _276;
			}
			if (_277 > 0) {
				_275 = _26d;
				window.setTimeout(f, 500);
			} else {
				for ( var n in _273) {
					YAHOO.log(n + " " + _273[n]);
				}
				self.verifyEndModule();
				self.dirty = false;
				self.restoring = false;
				self.onload.fire(false, "Failed to create all pipe modules",
						self);
			}
			return;
		}
		self.restoreWires(_26a);
		self.verifyEndModule();
		var _27a = self.restoreFieldState(_26a);
		if (!_27a) {
			self.fieldpreview();
		}
		self.dirty = false;
		self.restoring = false;
		self.onload.fire(true, self.pipe, self);
	};
	f();
};
maxwell.Editor.prototype.restoreFieldState = function(_27b) {
	if (!_27b.terminaldata) {
		return false;
	}
	var _27c = _27b.terminaldata;
	for ( var key in _27c) {
		var _27e = _27c[key];
		var m = this.getModuleById(_27e.moduleid);
		if (!m) {
			continue;
		}
		var t = m.getTerminalById(_27e.id);
		if (!t) {
			continue;
		}
		t.setPreviewData(_27e.data);
	}
	return true;
};
maxwell.Editor.prototype.restoreWires = function(_281) {
	for ( var key in _281.wires) {
		var wire = _281.wires[key];
		var msrc = this.getModuleById(wire.src.moduleid);
		var mtgt = this.getModuleById(wire.tgt.moduleid);
		var tsrc = null;
		var ttgt = null;
		for ( var i = 0; i < msrc.terminals.length; i++) {
			var id = msrc.terminals[i].computedId;
			if (id == wire.src.id) {
				tsrc = msrc.terminals[i];
				break;
			}
		}
		for ( var i = 0; i < mtgt.terminals.length; i++) {
			var id = mtgt.terminals[i].computedId;
			if (id == wire.tgt.id) {
				ttgt = mtgt.terminals[i];
				break;
			}
		}
		if (ttgt && tsrc) {
			var conf = {
				restoreTime : true
			};
			new maxwell.Wire(tsrc, ttgt, conf);
		} else {
			YAHOO.log("WARNING: Unable to reconnect wire " + wire.src.id
					+ " to " + wire.tgt.id);
		}
	}
};
maxwell.Editor.prototype.onNewLibraryModule = function(type, args, self) {
	var m = args[0];
	var _28f = m.tags.concat([ "moduletype" ]);
	var dd = new maxwell.DragDrop.Source($id(m.node), m.type, _28f);
};
maxwell.Editor.prototype.addModule = function(_291, xy) {
	this.layout.addBlock(_291, xy);
	_291.onchange.subscribe(this.onChange, this, true);
};
maxwell.Editor.prototype.remove = function() {
	if (this.removed) {
		return;
	}
	this.removed = true;
	this.onremove.fire(this);
	this.layoutTarget.unreg();
	this.layoutTarget = null;
	this.node.parentNode.removeChild(this.node);
};
$namespace("maxwell.PipesEditor");
maxwell.PipesEditor = function(tabs, _294) {
	if (maxwell.PipesEditor._instance) {
		return maxwell.PipesEditor._instance;
	}
	this.onseteditor = new YAHOO.util.CustomEvent("onseteditor", this, true);
	this.onneweditor = new YAHOO.util.CustomEvent("onneweditor", this, true);
	this.editors = [];
	this.editorIndex = -1;
	this.node = cn("div", {
		className : "pipeseditor"
	});
	this.editorsnode = cn("div", {
		className : "editorscontainer"
	});
	this.tabs = tabs;
	this.tabs.onopeneditor.subscribe(function(type, args, self) {
		maxwell.PipesEditor.ClearStatus();
		this.SetCurrentEditor(args[0]);
	}, this, true);
	this.tabs.onchangetitle.subscribe(function(type, args, self) {
		maxwell.PipesEditor.ClearStatus();
		args[0].pipe.name = args[1];
		args[0].dirty = true;
		this.tabs.updateTabText(args[0]);
		this.toolbar.update();
	}, this, true);
	this.status = cn(
			"ul",
			{
				className : "status"
			},
			null,
			"<li class='statusborder statusleft'></li><li class='statusborder statusbody'></li><li class='statusborder statusright'></li>");
	this.tabs.tabsholder.appendChild(this.status);
	this.toolbar = _294;
	this.library = new maxwell.ModuleLibrary();
	this.toolbar.onupdate.subscribe(function(type, args, me) {
		var _29e = args[0];
		_29e = this.editors[this.editorIndex];
		var pid = _29e.pipe.id;
		if (_29e.dirty) {
			maxwell.PipesEditor.ClearStatus();
		}
	}, this, true);
	this.onseteditor.subscribe(this.toolbar.updateInfo, this.toolbar, true);
	this.library.onnewlibrarymodule.subscribe(function(type, args, me) {
		var m = args[0];
		var _2a4 = m.tags.concat([ "moduletype" ]);
		var dd = new maxwell.DragDrop.Source($id(m.node), m.type, _2a4);
		var _2a6 = m.node.getElementsByTagName("div")[2];
		YAHOO.util.Event.addListener(_2a6, "click", function(e) {
			var xy = YAHOO.util.Event.getXY(e);
			var _2a9 = maxwell.PipesEditor.CurrentEditor();
			xy[0] += 100;
			_2a9.layoutTarget.onSourceDragDrop.fire(dd, xy);
		}, this, true);
	}, this, true);
	this.node.appendChild(this.library.node);
	this.node.appendChild(this.editorsnode);
	this.splitter = new maxwell.Splitter.LeftRight(this.library.node,
			this.editorsnode);
	document.body.appendChild(this.node);
	this.splitter.resizeAB();
	maxwell.PipesEditor._instance = this;
	window.onbeforeunload = function(e) {
		var _2ab = maxwell.PipesEditor._instance.getDirtyEditorNames();
		if (_2ab.length > 0) {
			return "You have unsaved changes to " + _2ab.join("\r\n")
					+ " that will be *lost*.";
		}
	};
};
maxwell.PipesEditor.SetRunPipeStatus = function(txt) {
	var pid = maxwell.PipesEditor.CurrentEditor().pipe.id;
	var _2ae = "/pipes/pipe.info?_id=" + pid;
	var _2af = "_run" + pid;
	var a = "<a class='runpipe' target='+runid+' href='" + _2ae
			+ "'>Run Pipe...</a>";
	maxwell.PipesEditor.SetStatus(a, txt);
};
maxwell.PipesEditor.ClearStatus = function(_2b1, _2b2) {
	var s = maxwell.PipesEditor._instance.status;
	if (s.style.display != "none") {
		var anim = new YAHOO.util.Anim(s, {
			opacity : {
				to : 0
			}
		}, 0.5, YAHOO.util.Easing.easeOut);
		anim.onComplete.subscribe(function(type, args, me) {
			s.style.display = "none";
			if (_2b1) {
				maxwell.PipesEditor.SetStatus(_2b1, _2b2);
			}
		}, this, true);
		anim.animate();
	}
};
maxwell.PipesEditor.SetStatus = function(_2b8, _2b9) {
	var s = maxwell.PipesEditor._instance.status;
	var _2bb = null;
	if (s.style.display == "block") {
		maxwell.PipesEditor.ClearStatus(_2b8, _2b9);
		return;
	}
	if (_2b9) {
		_2b8 = "<span class='statustitle'>" + _2b9 + "</span>" + _2b8;
	}
	var sb = s.getElementsByTagName("li")[1];
	sb.innerHTML = _2b8;
	if (s.style.display != "block") {
		YAHOO.util.Dom.setStyle(this.status, "opacity", 0);
		s.style.display = "block";
		var anim = new YAHOO.util.Anim(s, {
			opacity : {
				to : 1
			}
		}, 0.5, YAHOO.util.Easing.easeIn);
		anim.animate();
	}
	var _2be = YAHOO.util.Dom
			.getXY(maxwell.PipesEditor._instance.tabs.node.lastChild)[0];
	var left = YAHOO.util.Dom.getRegion(s.previousSibling).right;
	s.style.marginLeft = ((_2be - left - sb.offsetWidth) / 2) + "px";
};
maxwell.PipesEditor.UpdatePipeName = function() {
	return maxwell.PipesEditor._instance.UpdatePipeName();
};
maxwell.PipesEditor.prototype.UpdatePipeName = function() {
	this.tabs.updateTabText(this.CurrentEditor());
};
maxwell.PipesEditor.CurrentEditor = function() {
	return maxwell.PipesEditor._instance.CurrentEditor();
};
maxwell.PipesEditor.prototype.CurrentEditor = function() {
	if (this.editorIndex < 0) {
		return null;
	}
	return this.editors[this.editorIndex];
};
maxwell.PipesEditor.AddEditor = function(_2c0) {
	maxwell.PipesEditor._instance.AddEditor(_2c0);
};
maxwell.PipesEditor.prototype.SetCurrentEditor = function(_2c1) {
	if (typeof (_2c1) == "object") {
		for ( var i = 0; i < this.editors.length; i++) {
			if (this.editors[i] == _2c1) {
				_2c1 = i;
				break;
			}
		}
		if (typeof (_2c1) == "object") {
			return false;
		}
	}
	var _2c3 = null;
	if (this.editorIndex >= 0 && this.editorIndex < this.editors.length) {
		_2c3 = this.editors[this.editorIndex];
	}
	if (!this.CloseEditorsAfter(this.editors[_2c1])) {
		return false;
	}
	if (_2c3 && !_2c3.removed) {
		_2c3.node.style.display = "none";
		_2c3.layoutTarget.unreg();
		_2c3.layoutTarget = null;
	}
	this.editorIndex = _2c1;
	var ed = this.editors[_2c1];
	ed.node.style.display = "block";
	if (!ed.layoutTarget) {
		ed.initlayout();
	}
	this.onseteditor.fire(ed, _2c1);
	return true;
};
maxwell.PipesEditor.GetEditors = function() {
	return maxwell.PipesEditor._instance.GetEditors();
};
maxwell.PipesEditor.prototype.GetEditors = function() {
	return this.editors;
};
maxwell.PipesEditor.CloseEditorsAfter = function(_2c5) {
	return maxwell.PipesEditor._instance.CloseEditorsAfter(_2c5);
};
maxwell.PipesEditor.prototype.getDirtyEditorNames = function(_2c6) {
	if (!_2c6) {
		_2c6 = 0;
	}
	var _2c7 = [];
	for ( var j = _2c6; j < this.editors.length; j++) {
		if (this.editors[j].dirty) {
			_2c7.push(this.editors[j].pipe.name);
		}
	}
	return _2c7;
};
maxwell.PipesEditor.prototype.CloseEditorsAfter = function(_2c9) {
	for ( var i = 0; i < this.editors.length; i++) {
		if (this.editors[i] == _2c9) {
			var _2cb = this.getDirtyEditorNames(i + 1);
			if (_2cb.length > 0) {
				var txt = _2cb.join("\r\n");
				if (!confirm("Changes to " + txt
						+ " may be lost. Do you want to close this pipe?")) {
					return false;
				}
			}
			var _2cd = this.editors.length - 1;
			for ( var j = _2cd; j > i; j--) {
				_2c9 = this.editors[j];
				if (_2c9.pipe.id) {
					this.editorCache[_2c9.pipe.id] = _2c9;
				}
				this.editors.splice(j, 1);
				_2c9.remove();
			}
			return true;
		}
	}
	YAHOO.log("WARNING: editor close failed");
	return false;
};
maxwell.PipesEditor.prototype.editorCache = [];
maxwell.PipesEditor.CreateEditor = function(_2cf) {
	return maxwell.PipesEditor._instance.CreateEditor(_2cf);
};
maxwell.PipesEditor.prototype.CreateEditor = function(_2d0) {
	var _2d1 = null;
	if (_2d0) {
		if (this.editorCache[_2d0]) {
			_2d1 = this.editorCache[_2d0];
			_2d1.removed = false;
			this.AddEditor(_2d1, false);
		} else {
			_2d1 = new maxwell.Editor();
			this.AddEditor(_2d1, true);
			_2d1.load(_2d0);
		}
	} else {
		_2d1 = new maxwell.Editor();
		this.AddEditor(_2d1, true);
	}
	return _2d1;
};
maxwell.PipesEditor.prototype.AddEditor = function(_2d2, _2d3) {
	this.editors.push(_2d2);
	_2d2.node.style.visibility = "hidden";
	_2d2.initlayout();
	this.editorsnode.appendChild(_2d2.node);
	_2d2.node.style.display = "none";
	_2d2.node.style.visibility = "visible";
	this.tabs.addEditor(_2d2);
	this.tabs.openEditor(_2d2);
	_2d2.splitter.resizeAB();
	this.onneweditor.fire(_2d2);
};
maxwell.PipesEditor.FindEditorWithModule = function(_2d4) {
	return maxwell.PipesEditor._instance.FindEditorWithModule(_2d4);
};
maxwell.PipesEditor.prototype.FindEditorWithModule = function(_2d5) {
	for ( var i = 0; i < this.editors.length; i++) {
		var _2d7 = this.editors[i];
		if (_2d7.hasModule(_2d5)) {
			return _2d7;
		}
	}
	return null;
};
maxwell.PipesEditor.getClipTextArea = function() {
	var _2d8 = $("debugclip");
	if (!_2d8) {
		var _2d8 = cn("textarea", {
			id : "debugclip",
			noscroll : "true"
		}, {
			border : "3px solid orange",
			overflow : "hidden",
			color : "red",
			backgroundColor : "red",
			position : "absolute",
			bottom : "4px",
			right : "4px",
			width : "16px",
			height : "16px"
		});
		document.body.appendChild(_2d8);
	}
	return _2d8;
};
function clipout() {
	var frz = maxwell.PipesEditor.CurrentEditor().freeze();
	var _2da = maxwell.PipesEditor.getClipTextArea();
	_2da.value = $JSON(frz);
	_2da.focus();
	alert("Ctrl-A, Ctrl-C");
}
function clipin() {
	var _2db = maxwell.PipesEditor.getClipTextArea();
	_2db.value = "";
	_2db.focus();
	YAHOO.util.Event.purgeElement(_2db);
	YAHOO.util.Event.addListener(_2db, "blur", function() {
		maxwell.PipesEditor._instance.SetCurrentEditor(0);
		maxwell.PipesEditor.CurrentEditor().debugLoadFromJson(_2db.value);
	}, this, true);
	alert("Ctrl-V, tab");
}
$namespace("maxwell.Module");
maxwell.Module = function(_2dc) {
	maxwell.Module.superclass.constructor.call(this, _2dc);
	YAHOO.util.Dom.addClass(this.content, "module");
	this.type = _2dc.type;
	this.node.setAttribute("ismodule", "true");
	var self = this;
	this.onready = new YAHOO.util.CustomEvent("onready", this, true);
	this.onchange = new YAHOO.util.CustomEvent("onchange", this, true);
	this.onpreviewavailable = new YAHOO.util.CustomEvent("onpreviewavailable",
			this, true);
	this.onpreviewstart = new YAHOO.util.CustomEvent("onpreviewstart", this,
			true);
	this.onbeforewalk = new YAHOO.util.CustomEvent("onbeforewalk", this, true);
	this.onselect = new YAHOO.util.CustomEvent("onselect", this, true);
	this.onunselect = new YAHOO.util.CustomEvent("onunselect", this, true);
	this.ready = false;
	this.walker = new maxwell.HtmlPlus.Walker();
	this.walker.onchange.subscribe(function(type, args, self) {
		this.fireOnChange(args[0]);
	}, this, true);
	this.initWalker(this.walker);
	this.submodules = [];
	this.terminals = [];
	this.onnewterminal = new YAHOO.util.CustomEvent("onnewterminal", this, true);
	this.onremoveterminal = new YAHOO.util.CustomEvent("onremoveterminal",
			this, true);
	if (!this.config.isSubContent) {
		this.node.style.width = "180px";
	}
	if (this.config.name) {
		this.setTitle("<i>" + this.config.name + "</i>");
	}
	this.setBody("Creating module...");
	var info = maxwell.Module.getModuleInfo(this.type);
	if (info) {
		window.setTimeout(function() {
			self.init(info);
		}, 1);
	} else {
		ajaxCall("module/info", {
			"_out" : "json",
			"type" : this.type
		}, function(data) {
			if (data.info) {
				self.init(data.info);
			} else {
				self.init({
					name : "Failed to create " + self.type
				});
			}
		}, function(_2e3) {
			self.setBody("Failed: " + _2e3);
		});
	}
	$bind(this.node, this);
	this.onmovestart.subscribe(function() {
		this.dragging = true;
	}, this, true);
	this.onmoveend.subscribe(function() {
		this.dragging = false;
	}, this, true);
};
$extend(maxwell.Module, maxwell.SimpleWindow);
maxwell.Module.cache = [];
maxwell.Module.Create = function(type, _2e5) {
	var t = null;
	var _2e7 = "" + type;
	_2e7 = _2e7.replace(/\s+/g, "");
	_2e7 = _2e7.replace(/\:+/g, "");
	_2e7 = _2e7.replace(/\-+/g, "");
	_2e7 = _2e7.replace(/\.+/g, "");
	_2e7 = _2e7.replace(/\/+/g, "");
	_2e7 = _2e7.replace(/\++/g, "");
	var _2e8 = "maxwell.Module." + _2e7;
	var t = eval("typeof(" + _2e8 + ");");
	if (t == "undefined") {
		if ($endsWith(type, "input")) {
			_2e8 = "maxwell.Module.Input";
		} else {
			if (type.indexOf("pipe:") == 0) {
				_2e8 = "maxwell.Module.Pipe";
			} else {
				_2e8 = "maxwell.Module";
			}
		}
	}
	if (!_2e5) {
		_2e5 = {};
	}
	_2e5.type = type;
	_2e5.namespace = maxwell.PipesEditor.CurrentEditor().id;
	maxwell.tmpValue = _2e5;
	var _2e9 = eval("new " + _2e8 + "(maxwell.tmpValue);");
	return _2e9;
};
maxwell.Module.getModuleInfo = function(type) {
	var info = maxwell.Module.cache["info-" + type];
	return info;
};
maxwell.Module.setModuleInfo = function(type, info) {
	maxwell.Module.cache["info-" + type] = info;
};
maxwell.Module.getModule = function(node) {
	if (!node) {
		return null;
	}
	if (node == document.body) {
		return null;
	}
	if (node.getAttribute("ismodule")) {
		return $boundObject(node);
	}
	return maxwell.Module.getModule(node.parentNode);
};
maxwell.Module.prototype.getModuleById = function(id) {
	if (!this.submodules || this.submodules.length == 0) {
		return null;
	}
	for ( var i = 0; i < this.submodules.length; i++) {
		var sm = this.submodules[i];
		if (sm.id == id) {
			return sm;
		}
		var m = sm.getModuleById(id);
		if (m) {
			return m;
		}
	}
	return null;
};
maxwell.Module.prototype.hasModule = function(_2f3) {
	if (!this.submodules || this.submodules.length == 0) {
		return false;
	}
	for ( var i = 0; i < this.submodules.length; i++) {
		var sm = this.submodules[i];
		if (sm == _2f3) {
			return true;
		}
		var m = sm.hasModule(_2f3);
		if (m) {
			return true;
		}
	}
	return false;
};
maxwell.Module.prototype.getTerminalById = function(id) {
	for ( var i = 0; i < this.terminals.length; i++) {
		var t = this.terminals[i];
		if (t.id == id) {
			return t;
		}
	}
	return null;
};
maxwell.Module.prototype.initDragDrop = function() {
	if (this.config.isSubContent) {
		return;
	}
	maxwell.Module.superclass.initDragDrop.call(this);
	this.dd.addInvalidHandleClass("terminal");
	this.dd.addInvalidHandleClass("wire");
};
maxwell.Module.prototype.init = function(def) {
	maxwell.Module.setModuleInfo(this.type, def);
	var _2fb = null;
	if (this.node.parentNode && this.node.parentNode.tagName) {
		_2fb = this.node.cloneNode(true);
		_2fb.removeAttribute("id");
		this.node.parentNode.appendChild(_2fb);
	}
	sn(this.node, null, {
		visibility : "hidden"
	});
	this.def = def;
	this.setTitle(def.name);
	if (this.type == "output") {
		this.buttons.parentNode.removeChild(this.buttons);
		YAHOO.util.Dom.addClass(this.content, "moduleoutput");
	}
	if (!!this.def.terminals) {
		var _2fc = 0;
		var _2fd = 0;
		for ( var i = 0; i < this.def.terminals.length; i++) {
			if (this.def.terminals[i].input) {
				_2fc++;
				continue;
			}
			if (this.def.terminals[i].output) {
				_2fd++;
			}
		}
		var _2ff = function(node, _301, _302) {
			var _303 = 15;
			var _304 = 100 - 2 * _303;
			var perc = _301 * _304 / _302;
			var _306 = 50 + perc - _304 / 2 - _304 / (2 * _302);
			node.style.left = _306 + "%";
			node.style.right = "auto";
		};
		var _307 = 0;
		var _308 = 0;
		for ( var i = 0; i < this.def.terminals.length; i++) {
			var t = this.def.terminals[i];
			if (t.input) {
				_307++;
				if (t.input == "rss") {
					t.input = "items";
				}
				var te = new maxwell.InputTerminal({
					id : t.name,
					placement : "north",
					types : [ t.input ],
					singleWire : true
				});
				if (_2fc > 1) {
					_2ff(te.node, _307, _2fc);
				}
				this.addTerminal(te, true);
				te.node.setAttribute("title", "");
			}
			if (t.output) {
				if (t.output == "rss") {
					t.output = "items";
				}
				_308++;
				var conf = {
					id : t.name,
					placement : "south",
					types : [ t.output ]
				};
				conf.dynamicType = (t.output == "dynamic");
				conf.singleWire = (t.output == "items");
				var te = new maxwell.OutputTerminal(conf);
				if (_2fd > 0) {
					_2ff(te.node, _308, _2fd);
				}
				this.addTerminal(te, true);
				te.node.setAttribute("title", "");
			}
			if (this.config.isSubContent) {
				te.node.display = "none";
			}
		}
	}
	if (this.def.ui) {
		this.hide.style.display = "block";
		YAHOO.util.Dom.addClass(this.bd, "params");
		this.bd.innerHTML = "<table nowrap><tbody><tr nowrap><td nowrap><span><form>"
				+ this.def.ui + "</form></span></td></tr></tbody></table>";
		this.onbeforewalk.fire(this.walker);
		this.walker.walk(this.bd);
		this.showbd();
		var td = this.bd.getElementsByTagName("td")[0];
		var w = td.offsetWidth;
		if (!w) {
			w = 180;
		}
		if (maxwell.Util.ua.isSafari) {
			w += 56;
		} else {
			w += 18;
		}
		var w2 = this.hd.offsetWidth;
		var w = Math.max(w, w2);
		this.resize(w);
		this.bd.replaceChild(td.firstChild, this.bd.firstChild);
	} else {
		this.hide.style.display = "none";
		this.hidebd();
		this.bd.innerHTML = "";
	}
	if (this.config.conf) {
		this.restore(this.config.conf);
	} else {
		this.ready = true;
		this.onready.fire();
	}
	YAHOO.util.Event.addListener(this.node, "mousedown", this.select, true,
			this);
	sn(this.node, null, {
		visibility : "visible"
	});
	if (_2fb) {
		_2fb.parentNode.removeChild(_2fb);
	}
};
maxwell.Module.prototype.help = function(ev) {
	maxwell.PipesEditor._instance.library.showHelp(this.type, true);
};
maxwell.Module.prototype.select = function(e) {
	if (e) {
		var _311 = YAHOO.util.Event.getTarget(e);
		if (_311.tagName != "DIV" && _311.tagName != "SPAN") {
			return;
		}
		if (YAHOO.util.Dom.hasClass(_311, "enhancedtarget")) {
			return;
		}
	}
	maxwell.HtmlPlus.Util.CloseInputs();
	if (!this.selected) {
		this.selected = true;
		YAHOO.util.Dom.addClass(this.content, "moduleselect");
		YAHOO.util.Dom.addClass(this.node, "moduleselected");
		this.onselect.fire(this);
	}
};
maxwell.Module.prototype.unselect = function() {
	if (this.selected) {
		this.selected = null;
		YAHOO.util.Dom.removeClass(this.content, "moduleselect");
		YAHOO.util.Dom.removeClass(this.node, "moduleselected");
		this.onunselect.fire(this);
	}
};
maxwell.Module.prototype.initHeader = function() {
	maxwell.Module.superclass.initHeader.call(this);
	this.terminalContainer = cn("ul", {
		className : "terminallist"
	});
	this.hd.insertBefore(this.terminalContainer, this.buttons);
};
maxwell.Module.prototype.fireOnRemove = function() {
	for ( var i = this.terminals.length - 1; i >= 0; i--) {
		this.terminals[i].remove();
	}
	for ( var i = this.submodules.length - 1; i >= 0; i--) {
		this.submodules[i].remove();
	}
	maxwell.Module.superclass.fireOnRemove.call(this);
	$unbindObject(this);
};
maxwell.Module.prototype.fireOnHideShowBody = function(_313, _314) {
	if (!_314) {
		_314 = this.terminalContainer;
	}
	this.repositionInputTerminals(_313, _314);
	for ( var i = this.submodules.length - 1; i >= 0; i--) {
		this.submodules[i].repositionInputTerminals(_313, _314);
	}
	maxwell.Module.superclass.fireOnHideShowBody.call(this, _313);
};
maxwell.Module.prototype.fireOnMove = function(_316, _317) {
	var t = this.terminals;
	var l = this.terminals.length;
	for ( var i = 0; i < l; i++) {
		t[i].fireOnMove(null, _317);
	}
	this.onmove.fire(_316, _317);
};
maxwell.Module.prototype.fireOnChange = function(_31b) {
	this.lastChange = new Date();
	this.onchange.fire(this, _31b);
};
maxwell.Module.prototype.isPreviewStale = function() {
	if (!this.lastPreview) {
		return true;
	}
	if (!this.lastChange) {
		return false;
	}
	return (this.lastChange > this.lastPreview);
};
maxwell.Module.prototype.updateXY = function() {
	for ( var i = 0; i < this.terminals.length; i++) {
		if (this.terminals[i].wires.length > 0) {
			this.terminals[i].wires[0].updateXY();
		}
	}
	return maxwell.Module.superclass.updateXY.call(this);
};
maxwell.Module.prototype.restore = function(_31d) {
	this.submoduleCount = 0;
	this.rec_restore(_31d, this.node);
	var _31e = 500;
	var self = this;
	if (this.submoduleCount > 0) {
		var f = function() {
			if (self.submodules.length != self.submoduleCount) {
				_31e--;
				if (_31e > 0) {
					window.setTimeout(f, 100);
				} else {
					YAHOO.log("ERROR: failed to restore submodule");
				}
				return;
			}
			self.ready = true;
			self.onready.fire();
		};
		f();
	} else {
		this.ready = true;
		this.onready.fire();
	}
};
maxwell.Module.prototype.rec_restore = function(data, node) {
	if (!data || data == {}) {
		return;
	}
	if (!node.tagName) {
		return;
	}
	var tag = node.tagName.toLowerCase();
	var _324 = node.getAttribute("repeat");
	if (tag == "ul" && _324) {
		var key = node.getAttribute("key");
		if (key) {
			data = maxwell.Util.toList(data[key]);
		} else {
			var inp = node.getElementsByTagName("textarea");
			if (inp == null || inp.length == 0) {
				inp = node.getElementsByTagName("input");
				if (inp == null || inp.length == 0) {
					inp = node.getElementsByTagName("select");
					if (inp == null || inp.length == 0) {
						YAHOO
								.log("PANIC - no inputs/select element for repeating element");
						return;
					}
				}
			}
			key = inp[0].getAttribute("name");
			data = maxwell.Util.toList(data[key]);
			var tmp = [];
			for ( var i = 0; i < data.length; ++i) {
				var rec = {};
				rec[key] = data[i];
				tmp.push(rec);
			}
			data = tmp;
		}
		if (node.manager.size > data.length) {
			node.manager.clear();
		}
		for ( var i = node.manager.size; i < data.length; ++i) {
			node.manager.add();
		}
	} else {
		if (node.row && data.sort) {
			data = data.shift();
		} else {
			if (tag == "input" || tag == "select" || tag == "textarea") {
				if (node.type == "button") {
					return;
				}
				var key = node.getAttribute("name");
				if (node.type == "radio") {
				}
				var _32a = data[key];
				if (!_32a) {
					return;
				}
				var m = $boundObject(node);
				if (m) {
					var _32c = m.setValue(_32a);
					if (node.getAttribute("type") == "module" && _32c) {
						this.submoduleCount++;
					}
				} else {
					maxwell.Util.setInputValue(node, _32a.value);
				}
				return;
			}
		}
	}
	if (data) {
		for ( var i = 0; i < node.childNodes.length; ++i) {
			this.rec_restore(data, node.childNodes[i]);
		}
	}
};
maxwell.Module.prototype.freeze = function() {
	var _32d = {
		type : this.type,
		id : this.id,
		conf : {}
	};
	this.rec_freeze(_32d.conf, this.bd);
	return _32d;
};
maxwell.Module.prototype.rec_freeze = function(data, node) {
	if (!node.tagName) {
		return;
	}
	var tag = node.tagName.toLowerCase();
	var type = node.getAttribute("type");
	var key = node.getAttribute("key");
	var _333 = node.getAttribute("repeat");
	if ((tag == "ul") && key && _333) {
		data = data[key] = [];
	} else {
		if (node.row && data.sort) {
			var _334 = {};
			data.push(_334);
			data = _334;
		} else {
			if (tag == "input" || tag == "select" || tag == "textarea") {
				if (type == "button") {
					return;
				}
				var m = $boundObject(node);
				var val = null;
				if (m) {
					val = m.getValue();
				} else {
					if (type == "radio" || type == "checkbox") {
						if (node.checked != true) {
							return;
						}
					}
					var kind = node.getAttribute("kind");
					if (!kind) {
						kind = "text";
					}
					val = {
						type : kind,
						value : node.value
					};
				}
				if (!key) {
					key = node.getAttribute("name");
					if (type == "radio") {
					}
				}
				if (data[key]) {
					if (data[key].sort) {
						data[key].push(val);
					} else {
						data[key] = [ data[key], val ];
					}
				} else {
					data[key] = val;
				}
				return;
			}
		}
	}
	var _338 = (node.getAttribute("submodule") === "true");
	if (_338) {
		return;
	}
	for ( var i = 0; i < node.childNodes.length; ++i) {
		this.rec_freeze(data, node.childNodes[i]);
	}
};
maxwell.Module.prototype.initWalker = function(_33a) {
	_33a.onlistmanagerwalk.subscribe(function(type, args, me) {
		var list = args[0];
		list.onnewrow.subscribe(function(type, args, me) {
			this.walker.walk(args[0]);
			this.fireOnChange(list);
			if (maxwell.Util.ua.isSafari) {
				var t = this.getTerminalById("_OUTPUT");
				if (t) {
					t.node.style.width = t.node.offsetWidth + 1 + "px";
				}
			}
			window.setTimeout(function() {
				me.fireOnMove();
			}, 1);
		}, this, true);
		list.onremoverow.subscribe(function(type, args, me) {
			this.fireOnChange(list);
			if (maxwell.Util.ua.isSafari) {
				var t = this.getTerminalById("_OUTPUT");
				if (t) {
					t.node.style.width = t.node.offsetWidth - 1 + "px";
				}
			}
			window.setTimeout(function() {
				me.fireOnMove();
			}, 1);
		}, this, true);
		list.add();
	}, this, true);
	_33a.oncustominputwalk.subscribe(function(type, args, me) {
		var _34a = args[0];
		var node = args[1];
		if (_34a.kind == "module") {
			_34a.onsetmodule.subscribe(function(type, args, self) {
				this.submodules.push(args[1]);
				this.fireOnMove();
				this.fireOnChange(node);
			}, this, true);
			_34a.onclearmodule.subscribe(function(type, args, self) {
				for ( var i = 0; i < this.submodules.length; i++) {
					if (this.submodules[i].id == args[1].id) {
						this.submodules.splice(i, 1);
						break;
					}
				}
				this.fireOnMove();
				this.fireOnChange(node);
			}, this, true);
		}
		_34a.onchange.subscribe(function(type, args, me) {
			this.fireOnChange(args[0]);
		}, this, true);
	}, this, true);
	_33a.oninputwalk.subscribe(function(type, args, me) {
	}, this, true);
};
maxwell.Module.prototype.addTerminal = function(t, add) {
	this.terminals.push(t);
	t.block = this;
	if (add) {
		this.node.appendChild(t.node);
		t.computedId = t.id;
	}
	t.onremove.subscribe(function(type, args, self) {
		for ( var i = 0; i < this.terminals.length; i++) {
			if (this.terminals[i] == t) {
				this.terminals.splice(i, 1);
				this.onremoveterminal.fire(t);
				return;
			}
		}
	}, this, true);
	t.onnewwire.subscribe(this.onnewwire, this, true);
	t.onremovewire.subscribe(this.onremovewire, this, true);
	new maxwell.TerminalMenu(t);
	this.onnewterminal.fire(t);
	return t;
};
maxwell.Module.prototype.repositionInputTerminals = function(_35f, _360) {
	if (!_360) {
		_360 = this.terminalContainer;
	}
	var inps = this.bd.getElementsByTagName("input");
	for ( var i = 0; i < inps.length; i++) {
		var _363 = $boundObject(inps[i]);
		if (!_363) {
			continue;
		}
		if (!_363.internalTerminal || _363.internalTerminal.wires.length == 0) {
			continue;
		}
		if (!_35f) {
			var li = cn("li");
			li.appendChild(_363.internalTerminal.node);
			_360.appendChild(li);
			continue;
		}
		var li = _363.internalTerminal.node.parentNode;
		maxwell.Util.insertAfter(_363.internalTerminal.node, _363.node);
		_360.removeChild(li);
	}
};
maxwell.Module.prototype.onnewwire = function(type, args, self) {
	var wire = args[0];
	this.fireOnChange(wire);
};
maxwell.Module.prototype.onremovewire = function(type, args, self) {
	var wire = args[0];
	this.fireOnChange(wire);
};
maxwell.Module.prototype.previewstart = function() {
	this.previewData = null;
	this.onpreviewstart.fire();
};
maxwell.Module.prototype.preview = function(data, _36e) {
	this.previewData = data;
	this.errorData = _36e;
	if (data && data.prop) {
		for ( var i = 0; i < this.terminals.length; i++) {
			var t = this.terminals[i];
			var d = data.prop[t.id];
			if (d) {
				t.setPreviewData(d);
			} else {
				if (this.type == "split") {
					if ((t.id.substr(0, 7) == "_OUTPUT") && (t.id.length > 7)) {
						d = data.prop["_OUTPUT"];
						if (d) {
							t.setPreviewData(d);
						}
					}
				}
			}
		}
	}
	this.lastPreview = new Date();
	this.onpreviewavailable.fire(data, _36e);
};
maxwell.Module.prototype.hidebd = function(e, obj) {
	if (this.config.isSubContent) {
		return;
	}
	maxwell.Module.superclass.hidebd.call(this, e, obj);
};
maxwell.Module.prototype.showbd = function(e, obj) {
	if (this.config.isSubContent) {
		return;
	}
	maxwell.Module.superclass.showbd.call(this, e, obj);
};
$namespace("maxwell.Module.createrss");
maxwell.Module.createrss = function(_376) {
	maxwell.Module.createrss.superclass.constructor.call(this, _376);
};
$extend(maxwell.Module.createrss, maxwell.Module);
maxwell.Module.createrss.prototype.init = function(def) {
	maxwell.Module.createrss.superclass.init.call(this, def);
	var _378 = this.bd.getElementsByTagName("img");
	if (this.module) {
		for ( var x in this.module.conf) {
			if (x.indexOf("mediaContent") != -1) {
				if (this.module.conf[x].value) {
					this.hideshow(_378[0]);
					break;
				}
			}
		}
		for ( var y in this.module.conf) {
			if (y.indexOf("mediaThumb") != -1) {
				if (this.module.conf[y].value) {
					this.hideshow(_378[1]);
					break;
				}
			}
		}
	}
	YAHOO.util.Event.addListener(_378[0], "click", this.hideshow, this, true);
	YAHOO.util.Event.addListener(_378[1], "click", this.hideshow, this, true);
};
maxwell.Module.createrss.prototype.hideshow = function(e) {
	var _37c = YAHOO.util.Dom.getElementsByClassName("updating", "span",
			this.bd);
	if (!_37c.length) {
		var _37d = (YAHOO.util.Event.getTarget(e)) ? YAHOO.util.Event
				.getTarget(e) : e;
		var togg = (YAHOO.util.Dom.hasClass(_37d, "content")) ? "content_holder"
				: "thumb_holder";
		var _37f = YAHOO.util.Dom.getElementsByClassName(togg, "div", this.bd);
		if (YAHOO.util.Dom.hasClass(_37f, "mediahide")[0]) {
			YAHOO.util.Dom.replaceClass(_37f, "mediahide", "mediashow");
			YAHOO.util.Dom.replaceClass(_37d, "expandme", "closeme");
		} else {
			YAHOO.util.Dom.replaceClass(_37f, "mediashow", "mediahide");
			YAHOO.util.Dom.replaceClass(_37d, "closeme", "expandme");
		}
		this.fireOnMove();
	}
};
$namespace("maxwell.Module.Input");
maxwell.Module.Input = function(_380) {
	maxwell.Module.Input.superclass.constructor.call(this, _380);
	this.isInput = true;
	YAHOO.util.Dom.addClass(this.content, "moduleinput");
};
$extend(maxwell.Module.Input, maxwell.Module);
maxwell.Module.Input.InstanceCounters = [];
maxwell.Module.Input.prototype.init = function(def) {
	this.onbeforewalk.subscribe(this.stopInternalTerminals, this, true);
	maxwell.Module.Input.superclass.init.call(this, def);
	var inps = this.bd.getElementsByTagName("input");
	var name = inps[0];
	var _384 = inps[1];
	var n = $boundObject(name);
	var p = $boundObject(_384);
	var nv = n.getValue();
	var pv = p.getValue();
	if (nv.value == "" || pv.value == "") {
		var num = maxwell.Module.Input.InstanceCounters[this.type];
		if (!num) {
			num = 1;
		}
		maxwell.Module.Input.InstanceCounters[this.type] = num + 1;
		if (nv.value == "") {
			nv.value = this.type + num;
			n.setValue(nv);
		}
		if (pv.value == "") {
			pv.value = this.type + num;
			p.setValue(pv);
		}
	}
	YAHOO.util.Event.addListener(name, "keydown", this.nameKey, this, true);
	YAHOO.util.Event.addListener(_384, "keyup", this.promptKey, this, true);
	YAHOO.util.Event.addListener(_384, "blur", this.promptBlur, this, true);
	this.shortType = inps[4].getAttribute("novaluedisplay");
	if (!this.shortType) {
		this.shortType = "text";
	}
	this.setTitle(_384.value);
};
maxwell.Module.Input.prototype.nameKey = function(ev) {
	var k = YAHOO.util.Event.getCharCode(ev);
	if (k == 32 || (k >= 33 && k <= 45) || (k >= 112 && k <= 123)) {
		YAHOO.util.Event.stopEvent(ev);
		return;
	}
};
maxwell.Module.Input.prototype.promptKey = function(ev) {
	var _38d = YAHOO.util.Event.getTarget(ev);
	this.setTitle(_38d.value);
};
maxwell.Module.Input.prototype.promptBlur = function(ev) {
	var _38f = YAHOO.util.Event.getTarget(ev);
	if (_38f.value == "") {
		_38f.value = this.type;
	}
	this.setTitle(_38f.value);
};
maxwell.Module.Input.prototype.setTitle = function(_390) {
	if (this.shortType) {
		_390 += " (" + this.shortType + ")";
	}
	maxwell.Module.Input.superclass.setTitle.call(this, _390);
};
maxwell.Module.Input.prototype.addInputTerminal = function(node, type) {
};
maxwell.Module.Input.prototype.stopInternalTerminals = function(type, args, me) {
	this.walker.createCustomInput = function(node) {
		var _397 = {
			module : me,
			notwirable : true
		};
		return maxwell.HtmlPlus.Create(node, _397);
	};
};
$namespace("maxwell.Module.Pipe");
maxwell.Module.Pipe = function(_398) {
	_398.pipeid = _398.type.substr(_398.type.indexOf(":") + 1);
	maxwell.Module.setModuleInfo("pipe:new", {
		name : "New pipe",
		description : "New pipe",
		terminals : null,
		ui : "<i>Open this module to create UI</i>"
	});
	maxwell.Module.Pipe.superclass.constructor.call(this, _398);
};
$extend(maxwell.Module.Pipe, maxwell.Module);
maxwell.Module.Pipe.prototype.setTitle = function(_399) {
	if (!_399) {
		_399 = "Untitled";
	}
	if (this.openbutton) {
		YAHOO.util.Event.removeListener(this.openbutton
				.getElementsByTagName("a")[0], "click", this.openpipe, this,
				true);
		this.openbutton.parentNode.removeChild(this.openbutton);
	}
	maxwell.Module.Pipe.superclass.setTitle.call(this, _399);
	this.openbutton = cn("span", {
		className : "openpipe"
	}, null, "[<a href='#'>open</a>]&nbsp;");
	this.title.insertBefore(this.openbutton, this.title.firstChild);
	YAHOO.util.Event.addListener(this.openbutton.getElementsByTagName("a")[0],
			"click", this.openpipe, this, true);
};
maxwell.Module.Pipe.prototype.openpipe = function(e) {
	YAHOO.util.Event.stopEvent(e);
	var _39b = maxwell.PipesEditor.FindEditorWithModule(this);
	if (!maxwell.PipesEditor.CloseEditorsAfter(_39b)) {
		return;
	}
	var _39c = null;
	if (this.config.pipeid != "new") {
		_39c = maxwell.PipesEditor.CreateEditor(this.config.pipeid);
	} else {
		_39c = maxwell.PipesEditor.CreateEditor();
	}
	_39c.onsave.unsubscribe(this.updatepipe, this, true);
	_39c.onsave.subscribe(this.updatepipe, this, true);
};
maxwell.Module.Pipe.prototype.updatepipe = function(type, args, me) {
	if (!args[0]) {
		return;
	}
	var pipe = args[1];
	if (this.terminals && this.terminals.length > 0) {
		for ( var i = this.terminals.length - 1; i >= 0; i--) {
			this.terminals[i].remove();
		}
	}
	this.node.style.width = "180px";
	this.setBody("<i>Updating module...</i>");
	this.type = "pipe:" + pipe.id;
	this.config.pipeid = pipe.id;
	var self = this;
	ajaxCall("module/info", {
		"_out" : "json",
		"type" : this.type
	}, function(data) {
		if (data.info) {
			self.init(data.info);
		} else {
			self.setBody("Failed to update");
		}
	}, function(_3a4) {
		self.setBody("Failed to update " + _3a4);
	});
};
maxwell.Module.Pipe.prototype.freeze = function() {
	if (this.type == "pipe:new") {
		return null;
	}
	return maxwell.Module.Pipe.superclass.freeze.call(this);
};
$namespace("maxwell.Module.Pipe");
maxwell.Module.urlbuilder = function(_3a5) {
	maxwell.Module.urlbuilder.superclass.constructor.call(this, _3a5);
};
$extend(maxwell.Module.urlbuilder, maxwell.Module);
maxwell.Module.urlbuilder.prototype.initWalker = function(_3a6) {
	_3a6.onlistmanagerwalk.subscribe(function(type, args, self) {
		var list = args[0];
		if (list.node.getAttribute("key") == "PARAM") {
			this.list = list;
		}
	}, this, true);
	_3a6.oncustominputwalk.subscribe(function(type, args, self) {
		var inp = args[0];
		if (inp.input.getAttribute("name") == "BASE") {
			YAHOO.util.Event.addListener(inp.input, "change", this.processUrl,
					this, true);
		}
	}, this, true);
	maxwell.Module.urlbuilder.superclass.initWalker.call(this, _3a6);
};
maxwell.Module.urlbuilder.prototype.processUrl = function(ev) {
	var _3b0 = YAHOO.util.Event.getTarget(ev);
	var url = _3b0.value;
	var _3b2 = url.indexOf("?");
	if (_3b2 < 0) {
		return;
	}
	_3b0.value = url.substr(0, _3b2);
	var _3b3 = url.substr(_3b2 + 1).split("&");
	for ( var i = 0; i < _3b3.length; i++) {
		var _3b5 = _3b3[i].indexOf("=");
		var name = null;
		var val = null;
		if (_3b5 <= 0) {
			name = _3b3[i];
			val = "";
		} else {
			name = _3b3[i].substr(0, _3b5);
			val = _3b3[i].substr(_3b5 + 1);
		}
		var _3b8 = null;
		var _3b9 = this.list.node.childNodes;
		for ( var j = 0; j < _3b9.length; j++) {
			var li = _3b9[j];
			var _3bc = li.getElementsByTagName("input");
			if (_3bc.length != 2) {
				continue;
			}
			if (_3bc[0].value == name
					|| (_3bc[0].value == "" && _3bc[1].value == "")) {
				_3b8 = _3bc;
				break;
			}
		}
		if (_3b8 == null) {
			var li = this.list.add();
			_3b8 = li.getElementsByTagName("input");
		}
		maxwell.Util.setInputValue(_3b8[0], name);
		maxwell.Util.setInputValue(_3b8[1], decodeURI(val));
	}
};
$namespace("maxwell.Module.loop");
maxwell.Module.loop = function(_3bd) {
	maxwell.Module.loop.superclass.constructor.call(this, _3bd);
};
$extend(maxwell.Module.loop, maxwell.Module);
maxwell.Module.loop.prototype.init = function(def) {
	maxwell.Module.loop.superclass.init.call(this, def);
	var inps = this.bd.getElementsByTagName("select");
	this.ui_assignpart = inps[0];
	this.ui_emitpart = inps[1];
	if (this.submodules.length == 0) {
		this.hideUI();
	}
};
maxwell.Module.loop.prototype.initWalker = function(_3c0) {
	_3c0.oncustominputwalk.subscribe(function(type, args, me) {
		var _3c4 = args[0];
		var node = args[1];
		if (_3c4.kind == "module") {
			_3c4.onsetmodule.subscribe(function(type, args, self) {
				this.configureUI(args[1]);
			}, this, true);
			_3c4.onclearmodule.subscribe(function(type, args, self) {
				this.hideUI();
			}, this, true);
			return;
		}
		if (_3c4.kind == "field") {
			var n = _3c4.input.getAttribute("name");
			switch (n) {
			case "with":
				this.ui_with = _3c4;
				break;
			case "assign_to":
				this.ui_assign = _3c4;
				this.ui_assign.setEditable(true);
				break;
			}
		}
	}, this, true);
	maxwell.Module.loop.superclass.initWalker.call(this, _3c0);
};
maxwell.Module.loop.prototype.hideUI = function() {
	this.ui_assignpart.style.display = "none";
	this.ui_emitpart.style.display = "none";
	this.changeType("any");
	this.ui_with.disable();
};
maxwell.Module.loop.prototype.getModuleTerminalById = function(_3cd, id) {
	for ( var i = 0; i < _3cd.def.terminals.length; i++) {
		if (_3cd.def.terminals[i].name == id) {
			return _3cd.def.terminals[i];
		}
	}
	return null;
};
maxwell.Module.loop.prototype.configureUI = function(_3d0) {
	var _3d1 = this.getModuleTerminalById(_3d0, "_OUTPUT");
	var _3d2 = this.getModuleTerminalById(_3d0, "_INPUT");
	if (_3d1.output == "items") {
		this.ui_assignpart.style.display = "inline";
		this.ui_emitpart.style.display = "inline";
	} else {
		this.ui_assignpart.style.display = "none";
		this.ui_emitpart.style.display = "none";
	}
	if (_3d2) {
		var type = _3d2.input;
		if (this.ui_with.config.datatype != type) {
			this.changeType(type);
		}
		this.ui_with.enable();
	} else {
		this.ui_with.changeDataType("any");
		this.ui_with.setSelectedIndex(0);
		this.ui_with.disable();
	}
	if (this.ready) {
		this.ui_assign.setValue("item.loop:" + _3d0.def.type);
	}
};
maxwell.Module.loop.prototype.changeType = function(type) {
	this.ui_with.changeDataType(type);
	if (!this.ready) {
		return;
	}
	if (maxwell.HtmlPlus.Util.CanCast("text", type)) {
		var i = this.ui_with.getIndexOfValue("item.title");
		if (i != -1) {
			this.ui_with.setSelectedIndex(i);
		} else {
			this.ui_with.setSelectedIndex(0);
			this.ui_with.selectionDown();
		}
	} else {
		this.ui_with.setSelectedIndex(0);
		this.ui_with.selectionDown();
	}
};
$namespace("maxwell.Module.privatestring");
maxwell.Module.privatestring = function(_3d6) {
	maxwell.Module.privatestring.superclass.constructor.call(this, _3d6);
};
$extend(maxwell.Module.privatestring, maxwell.Module);
maxwell.Module.privatestring.prototype.init = function(def) {
	this.onbeforewalk.subscribe(this.stopInternalTerminals, this, true);
	maxwell.Module.privatestring.superclass.init.call(this, def);
};
maxwell.Module.privatestring.prototype.addInputTerminal = function(node, type) {
};
maxwell.Module.privatestring.prototype.stopInternalTerminals = function(type,
		args, me) {
	this.walker.createCustomInput = function(node) {
		var _3de = {
			module : me,
			notwirable : true
		};
		return maxwell.HtmlPlus.Create(node, _3de);
	};
};
$namespace("maxwell.Module.yql");
maxwell.Module.yql = function(_3df) {
	maxwell.Module.yql.superclass.constructor.call(this, _3df);
};
$extend(maxwell.Module.yql, maxwell.Module);
maxwell.Module.yql.prototype.init = function(def) {
	maxwell.Module.yql.superclass.init.call(this, def);
	var atag = this.bd.getElementsByTagName("a");
	this.txtarea = this.bd.getElementsByTagName("textarea");
	YAHOO.util.Event
			.addListener(atag[0], "click", this.openConsole, this, true);
};
maxwell.Module.yql.prototype.openConsole = function(q) {
	var theQ = encodeURIComponent(this.txtarea[0].value);
	var _3e4 = "http://developer.yahoo.com/yql/console?q=" + theQ;
	if (theQ == "text" || theQ == "") {
		_3e4 = "http://developer.yahoo.com/yql/console";
	}
	window.open(_3e4, "yqlConsole");
};
$namespace("maxwell.HtmlPlus.Util");
maxwell.HtmlPlus.Util.manageEnter = function(_3e5, _3e6) {
	YAHOO.util.Event.addListener(_3e5, "keydown", function(e) {
		var _3e8 = YAHOO.util.Event.getCharCode(e);
		if (_3e8 == 13) {
			_3e6(this);
		}
	}, _3e5, true);
};
maxwell.HtmlPlus.Util.manageInput = function(_3e9, _3ea) {
	var v = _3e9.value;
	if (v.value == "" || v == _3ea) {
		YAHOO.util.Dom.addClass(_3e9, "novalue");
		_3e9.value = _3ea;
	}
	_3e9.setAttribute("novaluedisplay", _3ea);
	YAHOO.util.Event.addListener(_3e9, "focus",
			maxwell.HtmlPlus.Util.inputFocus, _3e9, true);
	YAHOO.util.Event.addListener(_3e9, "blur", maxwell.HtmlPlus.Util.inputBlur,
			_3e9, true);
	_3e9.setValue = function(_3ec) {
		if (!_3ec || _3ec == "") {
			YAHOO.util.Dom.addClass(_3e9, "novalue");
			this.value = _3e9.getAttribute("novaluedisplay");
		} else {
			YAHOO.util.Dom.removeClass(this, "novalue");
			this.value = _3ec;
		}
	};
};
maxwell.HtmlPlus.Util.inputFocus = function(e) {
	if (YAHOO.util.Dom.hasClass(this, "novalue")) {
		YAHOO.util.Dom.removeClass(this, "novalue");
		this.value = "";
	}
};
maxwell.HtmlPlus.Util.inputBlur = function(e) {
	if (this.value.length == 0) {
		YAHOO.util.Dom.addClass(this, "novalue");
		this.value = this.getAttribute("novaluedisplay");
	}
};
maxwell.HtmlPlus.Util._from_to_casts = {
	"items" : [],
	"item" : [],
	"url" : [ "text" ],
	"location" : [ "text" ],
	"datetime" : [ "text" ],
	"number" : [ "text" ],
	"text" : [ "text", "number", "datetime" ],
	"time" : [ "text" ],
	"struct" : []
};
maxwell.HtmlPlus.Util._complex_types = [ "url", "location", "datetime",
		"struct" ];
maxwell.HtmlPlus.Util.IsValidType = function(type) {
	if (!type) {
		return false;
	}
	if (type == "any") {
		return true;
	}
	for ( var n in maxwell.HtmlPlus.Util._from_to_casts) {
		if (n == type) {
			return true;
		}
	}
	return false;
};
maxwell.HtmlPlus.Util.CanCast = function(_3f1, _3f2) {
	if (_3f1 == _3f2) {
		return true;
	}
	if (_3f2 == "any") {
		return true;
	}
	var cast = maxwell.HtmlPlus.Util._from_to_casts[_3f1];
	if (!cast) {
		return false;
	}
	if (cast.indexOf) {
		return (cast.indexOf(_3f2) >= 0);
	}
	for ( var i = 0; i < cast.length; i++) {
		if (cast[i] == _3f2) {
			return true;
		}
	}
	return false;
};
maxwell.HtmlPlus.Util.HasSubTypes = function(type) {
	if (maxwell.HtmlPlus.Util._complex_types.indexOf) {
		return (maxwell.HtmlPlus.Util._complex_types.indexOf(type) >= 0);
	}
	for ( var i = 0; i < maxwell.HtmlPlus.Util._complex_types.length; i++) {
		if (maxwell.HtmlPlus.Util._complex_types[i] == type) {
			return true;
		}
	}
	return false;
};
maxwell.HtmlPlus.Util.GetCastables = function(_3f7) {
	var _3f8 = [ _3f7 ];
	for ( var key in maxwell.HtmlPlus.Util._from_to_casts) {
		var cast = maxwell.HtmlPlus.Util._from_to_casts[key];
		if (cast.indexOf) {
			if (cast.indexOf(_3f7) >= 0) {
				_3f8.push(key);
			}
		} else {
			for ( var i = 0; i < cast.length; i++) {
				if (cast[i] == _3f7) {
					_3f8.push(key);
					break;
				}
			}
		}
	}
	return _3f8;
};
maxwell.HtmlPlus.Util.CloseInputs = function() {
	var i = $("hiddeninput");
	if (!i) {
		i = cn("input", {
			id : "hiddeninput"
		}, {
			display : "block",
			zoom : 1,
			position : "absolute",
			width : "1px",
			height : "1px",
			left : "-1000px",
			top : 0
		});
		$("header").appendChild(i);
	}
	i.focus();
	i.blur();
};
$namespace("maxwell.HtmlPlus.Input");
maxwell.HtmlPlus.Input = function(_3fd, _3fe) {
	this.config = _3fe;
	if (!this.config) {
		this.config = {};
	}
	this.input = _3fd;
	sn(this.input, {
		autocomplete : "off"
	});
	YAHOO.util.Dom.addClass(this.input, "htmlplusinput");
	this.node = cn("span", {
		className : "enhancedtarget"
	}, {
		display : "inline"
	}, "&nbsp;");
	if (this.config.nocontainer) {
		this.node.style.display = "none";
	} else {
		this.container = cn("div", {
			className : "enhancedarea",
			height : "auto",
			overflow : "auto"
		}, {
			display : "none",
			position : "absolute"
		});
		YAHOO.util.Event.onAvailable($id(this.input), function(self) {
			var p = this.input.offsetParent;
			while (p
					&& !(p.tagName == "DIV" && YAHOO.util.Dom.hasClass(p,
							"editcontainer"))) {
				p = p.offsetParent;
			}
			p.appendChild(this.container);
			YAHOO.util.Event.addListener(this.node, "click", this.trigger,
					this, true);
			YAHOO.util.Event.addListener(this.container, "mouseover",
					this.containerMouseOver, this, true);
			YAHOO.util.Event.addListener(this.container, "mouseout",
					this.containerMouseOut, this, true);
		}, this, true);
	}
	maxwell.Util.insertAfter(this.node, this.input);
	var self = this;
	window
			.setTimeout(
					function() {
						var w = (self.input.offsetWidth - (self.node.offsetWidth + 2));
						if (w > 0) {
							self.input.style.width = (self.input.offsetWidth - (self.node.offsetWidth + 2))
									+ "px";
						}
					}, 1);
	this.onchange = new YAHOO.util.CustomEvent("onchange", this, true);
	this.onchangefocus = new YAHOO.util.CustomEvent("onchangefocus", this, true);
	this.addInputListeners();
	this.hasFocus = false;
};
maxwell.HtmlPlus.Input.prototype.fireChange = function(e) {
	this.onchange.fire(this);
};
maxwell.HtmlPlus.Input.prototype.processKeyDown = function(e) {
};
maxwell.HtmlPlus.Input.prototype.trigger = function(e) {
	if (this.displayed) {
		this.close();
	} else {
		this.open();
	}
	YAHOO.util.Event.stopEvent(e);
};
maxwell.HtmlPlus.Input.prototype.containerMouseOver = function(e) {
	this.overContainer = true;
};
maxwell.HtmlPlus.Input.prototype.containerMouseOut = function(e) {
	this.overContainer = false;
};
maxwell.HtmlPlus.Input.prototype.startClose = function(e) {
	if (this.overContainer) {
		YAHOO.util.Event.stopEvent(e);
		if (maxwell.Util.ua.isSafari) {
			var self = this;
			window.setTimeout(function() {
				self.input.focus();
			}, 100);
		} else {
			this.input.focus();
		}
		return;
	}
	this.close();
	this.hasFocus = false;
	this.onchangefocus.fire(this.hasFocus);
};
maxwell.HtmlPlus.Input.prototype.close = function(_40a) {
	if (!this.displayed) {
		return;
	}
	if (!this.container) {
		return;
	}
	this.displayed = false;
	YAHOO.util.Dom.removeClass(this.node, "active");
	this.container.style.display = "none";
	this.overContainer = false;
	if (_40a) {
		this.input.focus();
	}
};
maxwell.HtmlPlus.Input.prototype.startOpen = function() {
	if (this.config.autoopen) {
		this.open();
	} else {
		this.hasFocus = true;
		this.onchangefocus.fire(this.hasFocus);
	}
};
maxwell.HtmlPlus.Input.prototype.open = function(_40b) {
	if (this.displayed) {
		return;
	}
	if (!this.node) {
		return;
	}
	this.displayed = true;
	this.overContainer = false;
	var r1 = YAHOO.util.Dom.getRegion(this.input);
	var r2 = YAHOO.util.Dom.getRegion(this.node);
	var w = (r2.right - r1.left) - 4;
	this.container.style.visibility = "hidden";
	this.container.style.display = "block";
	this.container.style.width = "auto";
	this.cleft = r1.left;
	this.ctop = Math.max(r1.bottom, r2.bottom) - 2;
	var self = this;
	window.setTimeout(function() {
		self.beforeOpen(w);
		if (self.container.style.width == "auto") {
			self.container.style.width = w + "px";
		}
		YAHOO.util.Dom.addClass(self.node, "active");
		YAHOO.util.Dom.setXY(self.container, [ self.cleft, self.ctop ]);
		self.container.style.visibility = "visible";
		if (!_40b) {
			self.input.focus();
		}
		self.hasFocus = true;
		self.onchangefocus.fire(self.hasFocus);
	}, 1);
};
maxwell.HtmlPlus.Input.prototype.beforeOpen = function() {
};
maxwell.HtmlPlus.Input.prototype.addInputListeners = function() {
	YAHOO.util.Event.addListener(this.input, "keydown", this.processKeyDown,
			this, true);
	YAHOO.util.Event.addListener(this.input, "blur", this.startClose, this,
			true);
	YAHOO.util.Event.addListener(this.input, "focus", this.startOpen, this,
			true);
	YAHOO.util.Event.addListener(this.input, "change", this.fireChange, this,
			true);
};
maxwell.HtmlPlus.Input.prototype.removeInputListeners = function() {
	YAHOO.util.Event.removeListener(this.input, "keydown", this.processKeyDown);
	YAHOO.util.Event.removeListener(this.input, "blur", this.startClose);
	YAHOO.util.Event.removeListener(this.input, "focus", this.startOpen);
	YAHOO.util.Event.removeListener(this.input, "change", this.fireChange);
};
maxwell.HtmlPlus.Input.prototype.remove = function() {
	if (this.config.nocontainer) {
	} else {
		YAHOO.util.Event.removeListener(this.node, "click", this.trigger);
		YAHOO.util.Event.removeListener(this.container, "mouseover",
				this.containerMouseOver);
		YAHOO.util.Event.removeListener(this.container, "mouseout",
				this.containerMouseOut);
		if (this.container.parentNode) {
			this.container.parentNode.removeChild(this.container);
		}
	}
	this.removeInputListeners();
};
maxwell.HtmlPlus.Input.prototype.getInputValue = function() {
	var val = this.input.getAttribute("val");
	if (val === null) {
		val = this.input.value;
		if (YAHOO.util.Dom.hasClass(this.input, "novalue")) {
			val = "";
		}
	}
	return {
		value : val,
		type : this.type
	};
};
maxwell.HtmlPlus.Input.prototype.setInputValue = function(_411) {
	if (typeof (_411.value) != "undefined") {
		maxwell.Util.setInputValue(this.input, _411.value);
	} else {
		maxwell.Util.setInputValue(this.input, _411);
	}
	if (_411.type) {
		this.type = _411.type;
	}
};
maxwell.HtmlPlus.Input.prototype.enable = function() {
	if (this.disabled) {
		this.input.removeAttribute("disabled");
		this.node.style.visibility = "visible";
		this.disabled = false;
	}
};
maxwell.HtmlPlus.Input.prototype.disable = function() {
	if (!this.disabled) {
		this.close();
		sn(this.input, {
			disabled : "true"
		});
		this.node.style.visibility = "hidden";
		this.disabled = true;
	}
};
$namespace("maxwell.HtmlPlus.WirableInput");
maxwell.HtmlPlus.Create = function(_412, _413) {
	var type = _412.getAttribute("type");
	return maxwell.HtmlPlus.CreateEx(type, _412, _413);
};
maxwell.HtmlPlus.CreateEx = function(type, _416, _417) {
	var t = null;
	if (!type) {
		return null;
	}
	var _419 = "maxwell.HtmlPlus.InputType." + type;
	var t = eval("typeof(" + _419 + ");");
	if (t == "undefined") {
		var _419 = "maxwell.HtmlPlus." + type;
		t = eval("typeof(" + _419 + ");");
		if (t == "undefined") {
			_419 = "maxwell.HtmlPlus.WirableInput";
		}
	}
	var _41a = null;
	if (!_417) {
		_417 = {};
	}
	_417.kind = type;
	maxwell.HtmlPlus.tmpValue = _417;
	maxwell.HtmlPlus.tmpValue2 = _416;
	var _41a = eval("new " + _419
			+ "(maxwell.HtmlPlus.tmpValue2,maxwell.HtmlPlus.tmpValue);");
	return _41a;
};
maxwell.HtmlPlus.WirableInput = function(_41b, _41c) {
	maxwell.HtmlPlus.WirableInput.superclass.constructor.call(this, _41b, _41c);
	this.onnewterminal = new YAHOO.util.CustomEvent("onnewterminal", this, true);
	this.onremoveterminal = new YAHOO.util.CustomEvent("onremoveterminal",
			this, true);
	this.type = this.config.type;
	if (!maxwell.HtmlPlus.Util.IsValidType(this.type)) {
		this.type = _41b.getAttribute("type");
		if (!maxwell.HtmlPlus.Util.IsValidType(this.type)) {
			this.type = this.config.kind;
			if (!maxwell.HtmlPlus.Util.IsValidType(this.type)) {
				this.type = "text";
			}
		}
	}
	this.kind = this.config.kind;
	YAHOO.util.Dom.addClass(_41b, this.kind + "input");
	if (!_41c.module) {
		this.module = maxwell.Module.getModule(this.node);
	} else {
		this.module = _41c.module;
	}
	this.internalTerminal = null;
	if (_41c.notwirable) {
	} else {
		this.internalTerminal = new maxwell.InputTerminal({
			placement : "inside",
			types : [ this.type ]
		});
		maxwell.Util.insertAfter(this.internalTerminal.node, this.node);
		this.getComputedId();
		this.module.addTerminal(this.internalTerminal);
		this.input.style.width = (this.input.offsetWidth - (this.internalTerminal.render.offsetWidth + 8))
				+ "px";
	}
	if (this.input.offsetWidth <= 64 && this.kind != "number") {
		this.input.style.width = "90px";
	}
	var m = $boundObject(this.input);
	if (!m) {
		this.bound = true;
		$bind(this.input, this);
	} else {
		this.bound = false;
	}
	if (this.internalTerminal) {
		this.internalTerminal.onnewwire.subscribe(this.handleAddWire, this,
				true);
		this.internalTerminal.onbeforenewwire.subscribe(
				this.handleBeforeAddWire, this, true);
		this.internalTerminal.onremovewire.subscribe(this.handleRemoveWire,
				this, true);
	}
};
$extend(maxwell.HtmlPlus.WirableInput, maxwell.HtmlPlus.Input);
maxwell.HtmlPlus.WirableInput.prototype.getValue = function() {
	if (this.internalTerminal && this.internalTerminal.wires.length > 0) {
		var val = {
			type : this.type,
			terminal : this.getComputedId(true)
		};
		if (this.wiredfield) {
			val.subkey = this.wiredfield.getValue().value;
		}
		return val;
	}
	return this.getInputValue();
};
maxwell.HtmlPlus.WirableInput.prototype.setValue = function(_41f) {
	if (_41f && _41f.terminal && _41f.terminal != "_INPUT") {
		if (_41f.subkey) {
			_41f.value = _41f.subkey;
			if (this.wiredfield) {
				this.wiredfield.setInputValue(_41f);
			} else {
				this.wiredValue = _41f;
			}
		} else {
		}
		return;
	}
	return this.setInputValue(_41f);
};
maxwell.HtmlPlus.WirableInput.prototype.enable = function() {
	if (this.disabled) {
		this.input.removeAttribute("disabled");
		this.node.style.visibility = "visible";
		this.disabled = false;
	}
};
maxwell.HtmlPlus.WirableInput.prototype.disable = function() {
	if (!this.disabled) {
		this.close();
		sn(this.input, {
			disabled : "true"
		});
		this.node.style.visibility = "hidden";
		this.disabled = true;
	}
};
maxwell.HtmlPlus.WirableInput.prototype.handleBeforeAddWire = function(type,
		args, me) {
	var w = args[0];
	if (w.src == this.terminal) {
		return;
	}
	this.unwiredValue = $clone(this.getValue());
};
maxwell.HtmlPlus.WirableInput.prototype.handleAddWire = function(type, args, me) {
	var w = args[0];
	if (w.src == this.internalTerminal) {
		return;
	}
	var _428 = w.tgt.types[0];
	var _429 = w.src.types[0];
	maxwell.Util.setInputValue(this.input, this.type + " [wired]");
	if (_428 == _429) {
		this.disable();
		return;
	}
	if (!maxwell.HtmlPlus.Util.HasSubTypes(_429)
			&& !maxwell.HtmlPlus.Util.HasSubTypes(_428)
			|| !maxwell.HtmlPlus.Util.HasSubTypes(_429)
			&& maxwell.HtmlPlus.Util.CanCast(_429, _428)) {
		this.disable();
		return;
	}
	this.close();
	this.removeInputListeners();
	this.oldnodestyle = YAHOO.util.Dom.getStyle(this.node, "display");
	this.node.style.display = "none";
	maxwell.Util.setInputValue(this.input, "");
	this.wiredfield = maxwell.HtmlPlus.CreateEx("field", this.input, {
		notwirable : true,
		terminal : this.internalTerminal,
		fieldtype : _429,
		datatype : _428
	});
	this.wiredfield.onchange.subscribe(this.refireChange, this, true);
	if (this.wiredValue) {
		this.wiredfield.setInputValue(this.wiredValue);
	}
	if (!w.config.restoreTime) {
		this.wiredfield.open();
	}
	this.onchange.fire(this);
};
maxwell.HtmlPlus.WirableInput.prototype.refireChange = function(type, args, me) {
	this.onchange.fire(args[0]);
};
maxwell.HtmlPlus.WirableInput.prototype.handleRemoveWire = function() {
	if (this.wiredfield) {
		this.wiredValue = this.wiredfield.getInputValue();
		this.wiredfield.remove();
		this.wiredfield.onchange.unsubscribe(this.refireChange, this);
		this.node.style.display = this.oldnodestyle;
	}
	this.wiredfield = null;
	this.enable();
	this.addInputListeners();
	this.onchange.fire(this);
	this.setInputValue(this.unwiredValue);
};
maxwell.HtmlPlus.WirableInput.prototype.remove = function() {
	maxwell.HtmlPlus.WirableInput.superclass.remove.call(this);
	if (this.internalTerminal) {
		this.internalTerminal.onnewwire.unsubscribe(this.handleAddWire, this);
		this.internalTerminal.onbeforenewwire.unsubscribe(
				this.handleBeforeAddWire, this);
		this.internalTerminal.onremovewire.unsubscribe(this.handleRemoveWire,
				this);
	}
	this.node.parentNode.removeChild(this.node);
	if (this.bound) {
		if (this.internalTerminal) {
			this.internalTerminal.remove();
		}
		$unbindNode(this.input);
	}
};
maxwell.HtmlPlus.WirableInput.prototype.getComputedId = function(_42d) {
	if (!this.computedId || _42d) {
		this.computedId = this.recomputeId(this.input);
		this.internalTerminal.computedId = this.computedId;
	}
	return this.computedId;
};
maxwell.HtmlPlus.WirableInput.prototype.recomputeId = function(node) {
	if (!node || node.nodeType != 1) {
		return "";
	}
	var _42f = this.recomputeId(node.parentNode);
	var key = null;
	if (node.tagName == "INPUT" || node.tagName == "SELECT"
			|| node.tagName == "TEXTAREA") {
		key = node.getAttribute("name");
	} else {
		if (node.getAttribute("key") && node.tagName != "FORM") {
			key = node.getAttribute("key");
		} else {
			if (node.tagName == "LI") {
				key = node.getAttribute("index");
			}
		}
	}
	return key ? (_42f ? _42f + "_" + key : key) : _42f;
};
$namespace("maxwell.HtmlPlus.DropDown");
maxwell.HtmlPlus.DropDown = function(_431, _432) {
	maxwell.HtmlPlus.DropDown.superclass.constructor.call(this, _431, _432);
	if (!this.config.numtoshow) {
		this.config.numtoshow = 10;
	}
	YAHOO.util.Event.addListener(this.input, "keypress", this.processKeyPress,
			this, true);
	if (this.config.editable === false) {
		YAHOO.util.Event.addListener(this.input, "click", this.startOpen, this,
				true);
		sn(this.input, {
			readonly : "true"
		}, {
			cursor : "pointer"
		});
		this.input.readOnly = true;
	}
	this.list = cn("ul", null, {
		listStyle : "none"
	});
	YAHOO.util.Event
			.addListener(this.list, "click", this.listclick, this, true);
	if (this.config.data) {
		this.populate(this.config.data, this.config.open);
	}
	this.container.appendChild(this.list);
	this.selectedIndex = -1;
};
$extend(maxwell.HtmlPlus.DropDown, maxwell.HtmlPlus.WirableInput);
maxwell.HtmlPlus.DropDown.prototype.remove = function() {
	maxwell.HtmlPlus.DropDown.superclass.remove.call(this);
	YAHOO.util.Event.removeListener(this.input, "keypress",
			this.processKeyPress);
	if (this.config.editable === false) {
		YAHOO.util.Event.removeListener(this.input, "click", this.startOpen);
		this.input.removeAttribute("readonly");
		this.input.style.cursor = "auto";
		this.input.readOnly = false;
	}
};
maxwell.HtmlPlus.DropDown.prototype.setEditable = function(_433) {
	if (_433 == this.editable) {
		return;
	}
	this.config.editable = _433;
	if (this.config.editable === false) {
		YAHOO.util.Event.addListener(this.input, "click", this.startOpen, this,
				true);
		sn(this.input, {
			readonly : "true"
		}, {
			cursor : "pointer"
		});
		this.input.readOnly = true;
	} else {
		YAHOO.util.Event.removeListener(this.input, "click", this.startOpen);
		this.input.removeAttribute("readonly");
		this.input.style.cursor = "auto";
		this.input.readOnly = false;
	}
};
maxwell.HtmlPlus.DropDown.prototype.beforeOpen = function(_434) {
	var n = this.list.childNodes[0];
	if (n) {
		if (!this.itemheight) {
			this.itemheight = n.offsetHeight;
			this.itemwidth = 0;
			for ( var i = 0; i < this.list.childNodes.length; i++) {
				if (this.list.childNodes[i].offsetWidth > this.itemwidth) {
					this.itemwidth = this.list.childNodes[i].offsetWidth;
				}
			}
		}
		if (this.itemwidth > _434) {
			this.container.style.width = (this.itemwidth + 18) + "px";
		}
		if (this.config.numtoshow
				&& this.list.childNodes.length > this.config.numtoshow) {
			this.container.style.overflow = "hidden";
			this.container.style.height = "auto";
			this.list.style.overflow = "auto";
			this.list.style.overflowX = "hidden";
			this.list.style.overflowY = "auto";
			this.list.style.height = this.itemheight * this.config.numtoshow
					+ "px";
		}
		this.list.style.zIndex = 1000;
		maxwell.Util.addShim(this.container, false);
		if (this.selectedIndex >= 0) {
			var li = this.list.childNodes[this.selectedIndex];
			var top = li.offsetTop;
			var _439 = top + li.offsetHeight;
			var _43a = this.list.scrollTop;
			var _43b = _43a + this.list.offsetHeight;
			if (top < _43a || _439 > _43b) {
				if (li.scrollIntoView && !maxwell.Util.ua.isSafari) {
					li.scrollIntoView();
				} else {
					this.list.scrollTop = top;
				}
			}
		}
	}
};
maxwell.HtmlPlus.DropDown.prototype.clear = function() {
	this.close();
	this.list.innerHTML = "";
	this.selectedIndex = -1;
	this.itemheight = 0;
};
maxwell.HtmlPlus.DropDown.prototype.populate = function(data, open) {
	this.clear();
	for ( var i in data) {
		var hit = data[i];
		this.add(hit);
	}
	if (open) {
		YAHOO.log("populate open "
				+ YAHOO.util.Dom.getStyle(this.node, "display"));
		this.open();
	}
};
maxwell.HtmlPlus.DropDown.prototype.odd = false;
maxwell.HtmlPlus.DropDown.prototype.add = function(data) {
	var _441 = "dropdownitem";
	if (this.odd) {
		_441 + " odd";
	}
	var name = data;
	var _443 = data;
	if (typeof (data) == "object") {
		name = data.name;
		_443 = data.value;
	}
	var n = cn("li", {
		className : _441,
		val : _443
	}, {
		display : "block",
		position : "relative",
		width : "100%"
	}, name);
	this.list.appendChild(n);
	this.odd = !this.odd;
	return n;
};
maxwell.HtmlPlus.DropDown.prototype.adjustOdd = function() {
	this.odd = false;
	for ( var i = 0; i < this.list.childNodes.length; i++) {
		var li = this.list.childNodes[i];
		if (!this.odd) {
			YAHOO.util.Dom.removeClass(li, "odd");
		} else {
			YAHOO.util.Dom.addClass(li, "odd");
		}
		this.odd = !this.odd;
	}
};
maxwell.HtmlPlus.DropDown.prototype.processKeyPress = function(e) {
	var k = e.keyCode;
	if (k == 40 || k == 38) {
		YAHOO.util.Event.stopEvent(e);
	}
};
maxwell.HtmlPlus.DropDown.prototype.processKeyDown = function(e) {
	var k = e.keyCode;
	switch (k) {
	case 40:
		this.selectionDown();
		break;
	case 38:
		this.selectionUp();
		break;
	case 13:
		this.close();
		break;
	default:
		this.startOpen();
		return;
	}
	YAHOO.util.Event.stopEvent(e);
};
maxwell.HtmlPlus.DropDown.prototype.listclick = function(e) {
	var tgt = YAHOO.util.Event.getTarget(e);
	while (tgt && tgt.tagName != "LI" && tgt != this.list) {
		tgt = tgt.parentNode;
	}
	if (!tgt || tgt == this.list) {
		return;
	}
	if (YAHOO.util.Dom.hasClass(tgt, "disabled")) {
		return;
	}
	for ( var i = 0; i < this.list.childNodes.length; i++) {
		if (tgt == this.list.childNodes[i]) {
			this.setSelectedIndex(i);
			var tmp = this.config.autoopen;
			this.config.autoopen = false;
			this.close(true);
			this.config.autoopen = tmp;
			YAHOO.util.Event.stopEvent(e);
			return;
		}
	}
};
maxwell.HtmlPlus.DropDown.prototype.selectionUp = function() {
	for ( var i = this.selectedIndex - 1; i >= 0; i--) {
		var li = this.list.childNodes[i];
		if (li.style.display == "none") {
			continue;
		}
		if (YAHOO.util.Dom.hasClass(li, "disabled")) {
			continue;
		}
		this.setSelectedIndex(i);
		return;
	}
};
maxwell.HtmlPlus.DropDown.prototype.selectionDown = function() {
	for ( var i = this.selectedIndex + 1; i < this.list.childNodes.length; i++) {
		var li = this.list.childNodes[i];
		if (li.style.display == "none") {
			continue;
		}
		if (YAHOO.util.Dom.hasClass(li, "disabled")) {
			continue;
		}
		this.setSelectedIndex(i);
		return;
	}
};
maxwell.HtmlPlus.DropDown.prototype.setSelectedIndex = function(_453, _454) {
	if (_453 == this.selectedIndex) {
		return false;
	}
	if (this.selectedIndex >= 0) {
		var li = this.list.childNodes[this.selectedIndex];
		YAHOO.util.Dom.removeClass(li, "selected");
	}
	if (_453 >= this.list.childNodes.length || _453 < 0) {
		this.selectedIndex = -1;
		return false;
	}
	var li = this.list.childNodes[_453];
	YAHOO.util.Dom.addClass(li, "selected");
	li.style.display = "block";
	this.selectedIndex = _453;
	if (!_454) {
		maxwell.Util.setInputValue(this.input, maxwell.Util.getInnerText(li));
	}
	this.onchange.fire(this);
	return true;
};
maxwell.HtmlPlus.DropDown.prototype.selectRange = function(_456, _457) {
	this.selectStart = _456;
	if (this.input.createTextRange) {
		var _458 = this.input.createTextRange();
		_458.moveStart("character", _456);
		_458.moveEnd("character", _457 - this.input.value.length);
		_458.select();
	} else {
		if (this.input.setSelectionRange) {
			this.input.setSelectionRange(_456, _457);
		}
	}
	this.input.focus();
};
maxwell.HtmlPlus.DropDown.prototype.getIndexOfValue = function(val) {
	for ( var i = 0; i < this.list.childNodes.length; i++) {
		var li = this.list.childNodes[i];
		var val2 = maxwell.Util.getInnerText(li);
		if (val2 == val) {
			return i;
		}
	}
	return -1;
};
maxwell.HtmlPlus.DropDown.prototype.setInputValue = function(val) {
	for ( var i = 0; i < this.list.childNodes.length; i++) {
		var li = this.list.childNodes[i];
		var val2 = maxwell.Util.getInnerText(li);
		if (val2 == val.value) {
			var set = this.setSelectedIndex(i);
			if (!set) {
				maxwell.HtmlPlus.DropDown.superclass.setInputValue.call(this,
						val);
			}
			return;
		}
	}
	this.selectedIndex = -1;
	maxwell.HtmlPlus.DropDown.superclass.setInputValue.call(this, val);
};
$namespace("maxwell.HtmlPlus.AutoComplete");
maxwell.HtmlPlus.AutoComplete = function(_462, _463) {
	maxwell.HtmlPlus.AutoComplete.superclass.constructor.call(this, _462, _463);
	YAHOO.util.Event.addListener(this.input, "keyup", this.processKeyUp, this,
			true);
};
$extend(maxwell.HtmlPlus.AutoComplete, maxwell.HtmlPlus.DropDown);
maxwell.HtmlPlus.AutoComplete.prototype.filter = function(v) {
	var _465 = null;
	var _466 = 0;
	for ( var i = 0; i < this.list.childNodes.length; i++) {
		var li = this.list.childNodes[i];
		var txt = maxwell.Util.getInnerText(li);
		var _46a = (txt.indexOf(v) == 0);
		if (_465 == null && _46a) {
			_465 = {
				index : i,
				txt : txt
			};
		}
		if (_46a) {
			_466++;
		}
		li.style.display = _46a ? "block" : "none";
	}
	this.adjustOdd();
	return {
		tophit : _465,
		count : _466
	};
};
maxwell.HtmlPlus.AutoComplete.prototype.setSelectedIndex = function(_46b, _46c) {
	var r = maxwell.HtmlPlus.AutoComplete.superclass.setSelectedIndex.call(
			this, _46b, _46c);
	if (_46c) {
		return r;
	}
	if (this.selectedIndex == -1) {
	} else {
		this.lastfiltervalue = maxwell.Util
				.getInnerText(this.list.childNodes[this.selectedIndex]);
	}
};
maxwell.HtmlPlus.AutoComplete.prototype.processKeyUp = function(e) {
	var k = YAHOO.util.Event.getCharCode(e);
	if (this.lastfiltervalue && this.lastfiltervalue == this.input.value) {
		return;
	}
	var _470 = true;
	if (this.lastfiltervalue
			&& this.lastfiltervalue.indexOf(this.input.value) == 0) {
		_470 = false;
	}
	this.lastfiltervalue = this.input.value;
	var _471 = this.filter(this.input.value);
	if (_471.tophit) {
		if (_471.tophit.txt == this.input.value) {
			this.setSelectedIndex(_471.tophit.index, true);
		} else {
			this.setSelectedIndex(-1, true);
		}
		if (_470) {
			var txt = _471.tophit.txt;
			this.typeAhead(txt);
		}
	} else {
		this.setSelectedIndex(-1, true);
	}
};
maxwell.HtmlPlus.AutoComplete.prototype.typeAhead = function(_473) {
	if (this.input.createTextRange || this.input.setSelectionRange) {
		var iLen = this.input.value.length;
		if (iLen == _473.length) {
			if (this.selectStart && this.selectStart < iLen) {
				iLen = this.selectStart;
			}
		}
		this.input.value = _473;
		this.selectRange(iLen, _473.length);
	}
};
$namespace("maxwell.HtmlPlus.Walker");
maxwell.HtmlPlus.Walker = function() {
	this.onlistmanagerwalk = new YAHOO.util.CustomEvent("onlistmanagerwalk",
			this, true);
	this.oncustominputwalk = new YAHOO.util.CustomEvent("oncustominputwalk",
			this, true);
	this.oninputwalk = new YAHOO.util.CustomEvent("oninputwalk", this, true);
	this.onchange = new YAHOO.util.CustomEvent("onchange", this, true);
};
maxwell.HtmlPlus.Walker.prototype.walk = function(node) {
	if (node.nodeType != 1) {
		return;
	}
	if (node.getAttribute("walked") === "true") {
		return;
	}
	if (node.getAttribute && node.getAttribute("nowalk") === "true") {
		return;
	}
	if (node.getAttribute && node.getAttribute("repeat") === "true") {
		var list = new maxwell.HtmlPlus.ListManager(node);
		this.onlistmanagerwalk.fire(list);
		return;
	}
	if (node.tagName == "TEXTAREA") {
		this.walkInput(node);
		return;
	}
	if (node.tagName == "INPUT") {
		this.walkInput(node);
		return;
	}
	if (node.tagName == "SELECT" || node.tagName == "INPUT") {
		YAHOO.util.Event.addListener(node, "change", function(e) {
			this.onchange.fire(YAHOO.util.Event.getTarget(e));
		}, this, true);
		return;
	}
	for ( var i = node.childNodes.length - 1; i >= 0; i--) {
		this.walk(node.childNodes[i]);
	}
};
maxwell.HtmlPlus.Walker.prototype.walkInput = function(node) {
	var type = node.getAttribute("type");
	if (!type || type == "radio" || type == "checkbox") {
		this.oninputwalk.fire(node, type);
		return;
	}
	var _47b = node.getAttribute("walked");
	if (!_47b) {
		node.setAttribute("walked", "true");
		var _47c = this.createCustomInput(node);
		if (!_47c.expandedNode) {
			this.oncustominputwalk.fire(_47c, node);
		} else {
			this.walk(_47c.expandedNode);
		}
	}
};
maxwell.HtmlPlus.Walker.prototype.createCustomInput = function(node) {
	return maxwell.HtmlPlus.Create(node);
};
$namespace("maxwell.HtmlPlus.ListManager");
maxwell.HtmlPlus.ListManager = function(node) {
	this.size = 0;
	var key = node.getAttribute("key");
	if (!key) {
		YAHOO
				.log("WARNING: no key for repeating element, trying to find a named input");
		var inps = node.getElementsByTagName("input");
		if (inps != null && inps.length > 0) {
			for ( var i = 0; i < inps.length; i++) {
				key = inps[i].getAttribute("name");
				if (key) {
					break;
				}
			}
		}
		if (!key) {
			YAHOO.log("PANIC: no key or name for repeating elements");
			key = "key" + node.innerHTML.length;
		}
		this.key = key;
		key = null;
	}
	var lab = node.getAttribute("label");
	if (!lab) {
		lab = "Add";
	}
	this.addNode = cn("div", null, null, "&nbsp;" + lab);
	var _483 = cn("img", {
		className : "paramadd",
		src : "http://l.yimg.com/a/i/space.gif",
		width : "12px",
		height : "16px"
	}, {
		width : "12px",
		height : "16px"
	});
	this.addNode.insertBefore(_483, this.addNode.firstChild);
	YAHOO.util.Event.addListener(_483, "click", this.add, this, true);
	this.node = cn("div");
	var ul = cn("ul", {
		repeat : "true",
		className : "paramlist"
	});
	this.node = ul;
	if (key) {
		sn(ul, {
			key : key
		});
	}
	ul.manager = this;
	node.removeAttribute("key");
	node.removeAttribute("repeat");
	node.parentNode.insertBefore(this.addNode, node);
	node.parentNode.replaceChild(this.node, node);
	this.template = node.cloneNode(true);
	this.onnewrow = new YAHOO.util.CustomEvent("onnewrow", this);
	this.onremoverow = new YAHOO.util.CustomEvent("onremoverow", this);
};
maxwell.HtmlPlus.ListManager.prototype.add = function() {
	this.size++;
	var _485 = this.template.cloneNode(true);
	var del = cn("img", {
		className : "paramdel",
		src : "http://l.yimg.com/a/i/space.gif",
		width : "12px",
		height : "16px"
	}, {
		width : "12px",
		height : "16px"
	});
	_485.insertBefore(del, _485.firstChild);
	var li = cn("li", {
		index : this.size,
		className : "row"
	}, null);
	li.appendChild(_485);
	li.row = true;
	this.node.appendChild(li);
	YAHOO.util.Event.addListener(del, "click", this.clickdel, this, true);
	this.onnewrow.fire(li);
	return li;
};
maxwell.HtmlPlus.ListManager.prototype.clickdel = function(e) {
	var li = YAHOO.util.Event.getTarget(e);
	li = li.parentNode.parentNode;
	var _48a = parseInt(li.getAttribute("index"), 10);
	for ( var i = _48a; i < this.node.childNodes.length; i++) {
		sn(this.node.childNodes[i], {
			index : i
		});
	}
	$unbindChildren(li);
	li.parentNode.removeChild(li);
	this.size--;
	this.onremoverow.fire(li, _48a - 1);
};
maxwell.HtmlPlus.ListManager.prototype.clear = function() {
	while (this.size) {
		var _48c = this.node.childNodes[0];
		var _48d = parseInt(_48c.getAttribute("index"), 10);
		$unbindChildren(_48c);
		this.onremoverow.fire(_48c);
		this.node.removeChild(_48c);
		this.size--;
	}
};
$namespace("maxwell.HtmlPlus.InputType.basefield");
maxwell.HtmlPlus.InputType.basefield = function(_48e, _48f) {
	if (!_48f.module) {
		_48f.module = maxwell.Module.getModule(_48e);
	}
	if (!_48f.terminal) {
		if (_48e.getAttribute("terminal")) {
			_48f.terminal = _48f.module.getTerminalById(_48e
					.getAttribute("terminal"));
		}
		if (!_48f.terminal) {
			_48f.terminal = _48f.module.getTerminalById("_INPUT");
		}
	}
	if (!_48f.editable) {
		_48f.editable = false;
	}
	_48f.autoopen = true;
	this.terminal = _48f.terminal;
	maxwell.HtmlPlus.InputType.basefield.superclass.constructor.call(this,
			_48e, _48f);
	this.updating = {};
	this.updateCount = 0;
	if (!maxwell.HtmlPlus.Util.IsValidType(this.config.fieldtype)) {
		this.config.fieldtype = "item";
	}
	if (!this.config.datatype) {
		if (_48e.getAttribute("datatype")) {
			this.config.datatype = _48e.getAttribute("datatype");
		}
		if (!this.config.datatype || this.config.datatype == "undefined") {
			this.config.datatype = "text";
		}
	}
	this.terminal.onpreviewavailable.subscribe(
			this.updateFieldNamesFromPreview, this, true);
	this.terminal.onfieldpreviewstart.subscribe(this.startFieldUpdate, this,
			true);
	this.terminal.onfieldpreviewavailable.subscribe(this.fieldUpdate, this,
			true);
	this.terminal.onfieldpreviewend.subscribe(this.endFieldUpdate, this, true);
	var pd = this.terminal.previewData;
	if (pd) {
		this.updateFieldNamesFromPreview(null, [ pd ], this);
	} else {
		this
				.updateFieldNamesFromPreview(
						null,
						[ maxwell.HtmlPlus.InputType.basefield.defaults[this.config.fieldtype] ],
						this);
	}
};
$extend(maxwell.HtmlPlus.InputType.basefield, maxwell.HtmlPlus.DropDown);
maxwell.HtmlPlus.InputType.basefield.prototype.remove = function() {
	maxwell.HtmlPlus.InputType.basefield.superclass.remove.call(this);
	this.terminal.onpreviewavailable.unsubscribe(
			this.updateFieldNamesFromPreview, this);
	this.terminal.onfieldpreviewstart.unsubscribe(this.startFieldUpdate, this);
	this.terminal.onfieldpreviewavailable.unsubscribe(this.fieldUpdate, this);
	this.terminal.onfieldpreviewend.unsubscribe(this.endFieldUpdate, this);
};
maxwell.HtmlPlus.InputType.basefield.defaults = {
	item : {
		_type : "item",
		_attr : {
			title : {},
			description : {},
			link : {
				_type : "url"
			},
			author : {},
			pubDate : {
				_type : "datetime"
			}
		}
	},
	url : {
		_type : "url",
		_attr : {
			host : {},
			scheme : {},
			port : {
				_type : "number"
			},
			querystring : {},
			path : {}
		}
	},
	location : {
		_type : "location",
		_attr : {
			street : {},
			city : {},
			state : {},
			postal : {},
			country : {},
			lat : {},
			lon : {},
			quality : {
				_type : "number"
			}
		}
	},
	datetime : {
		_type : "datetime",
		_attr : {
			timezone : {},
			hour : {
				_type : "number"
			},
			minute : {
				_type : "number"
			},
			second : {
				_type : "number"
			},
			year : {
				_type : "number"
			},
			month : {
				_type : "number"
			},
			day : {
				_type : "number"
			},
			day_of_week : {},
			utime : {
				_type : "number"
			}
		}
	}
};
maxwell.HtmlPlus.InputType.basefield.prototype.updateFieldNamesFromPreview = function(
		type, args, self) {
	var _494 = args[0];
	if (_494._type == "blank") {
		if (this.list.childNodes.length == 0) {
			this
					.updateFieldNamesFromPreview(
							type,
							[ maxwell.HtmlPlus.InputType.basefield.defaults[this.config.fieldtype] ],
							this);
		}
		return;
	}
	if (!_494._attr
			&& maxwell.HtmlPlus.InputType.basefield.defaults[_494._type]) {
		_494 = maxwell.HtmlPlus.InputType.basefield.defaults[_494._type];
	}
	var _495 = function(a, b) {
		var _498 = function(a) {
			if (!a._count) {
				a._count = 0;
			}
			if (!a._type || a._type.length == 0 || a._type == "blank") {
				if (a._attr) {
					a._type = "struct";
				} else {
					a._type = "text";
				}
			}
			if (!a._lname) {
				a._lname = a._name.toLowerCase();
			}
		};
		_498(a);
		_498(b);
		if (a._lname < b._lname) {
			return -1;
		}
		if (a._lname > b._lname) {
			return 1;
		}
		if (a._count > b._count) {
			return -1;
		}
		if (a._count < b._count) {
			return 1;
		}
		return 0;
	};
	var _49a = function(_49b, _49c) {
		var _49d = [];
		if (!_49b) {
			return _49d;
		}
		if (!_49c) {
			_49c = _49b._type;
		}
		if (!_49b._name) {
			_49b._name = "";
		}
		var a = [];
		for ( var key in _49b._attr) {
			var attr = _49b._attr[key];
			if (!attr) {
				attr = {};
			}
			attr._name = key;
			a.push(attr);
		}
		a.sort(_495);
		_49b._attr = a;
		var _4a1 = {
			type : _49b._type,
			value : _49c,
			name : _49c,
			prefix : ""
		};
		_49d.push(_4a1);
		flatten2 = [];
		for ( var i = 0; i < _49b._attr.length; i++) {
			var attr = _49b._attr[i];
			if (attr._attr) {
				flatten2 = flatten2.concat(_49a(attr, _49c + "." + attr._name));
			} else {
				_49d.push({
					value : _49c + "." + attr._name,
					type : attr._type,
					name : attr._name,
					prefix : _49c + "."
				});
			}
		}
		_49d = _49d.concat(flatten2);
		return _49d;
	};
	this.clear();
	var data = _49a($clone(_494));
	var val = this.getInputValue();
	this.populate(data);
	this.setInputValue(val);
	if (this.selectedIndex == -1 && (!val.subkey || val.subkey.length == 0)
			&& (!val.value || val.value.length == 0) && !this.config.editable) {
		this.selectionDown();
	}
};
maxwell.HtmlPlus.InputType.basefield.prototype.startFieldUpdate = function(
		type, args, me) {
	var _4a8 = args[0];
	if (this.updating[_4a8] && this.updating[_4a8] !== false) {
		return;
	}
	this.updating[_4a8] = true;
	this.updateCount++;
	if (this.updateCount > 1) {
		return;
	}
	this.updatingnode = cn(
			"span",
			{
				className : "updating",
				title : "Recomputing the possible field values based on the data flowing through this module"
			}, {
				top : this.input.offsetTop + "px",
				left : this.input.offsetLeft + "px",
				width : this.input.offsetWidth + "px",
				lineHeight : this.input.offsetHeight + "px",
				height : this.input.offsetHeight + "px"
			}, "Updating...");
	this.input.style.visibility = "hidden";
	if (me.module.config.type === "createrss") {
		var _4a9 = this.input.parentNode.parentNode;
		if (_4a9.className.indexOf("mediahide") != -1) {
			this.updatingnode.style.visibility = "hidden";
		}
		if (maxwell.Util.ua.isIE) {
			if (_4a9.className.indexOf("mediashow") != -1) {
				this.updatingnode.style.top = (_4a9.offsetTop + this.input.offsetTop)
						+ "px";
				this.updatingnode.style.left = (this.input.offsetLeft + 7)
						+ "px";
			}
		}
	}
	this.input.parentNode.insertBefore(this.updatingnode, this.input);
};
maxwell.HtmlPlus.InputType.basefield.prototype.fieldUpdate = function(type,
		args, me) {
	var _4ad = args[0];
	if (this.updating[_4ad] && this.updating[_4ad] !== false) {
		var data = args[1];
		this.updating[_4ad] = data;
	}
};
maxwell.HtmlPlus.InputType.basefield.prototype.endFieldUpdate = function(type,
		args, me) {
	var _4b2 = args[0];
	if (!this.updating[_4b2] || this.updating[_4b2] === false) {
		return;
	}
	var data = this.updating[_4b2];
	if (data && data !== true) {
		this.updateFieldNamesFromPreview(type, [ data ], me);
	} else {
	}
	this.updating[_4b2] = false;
	this.updateCount--;
	if (this.updateCount > 0) {
		return;
	}
	this.input.style.visibility = "visible";
	this.updatingnode.parentNode.removeChild(this.updatingnode);
	this.updatingnode = null;
};
maxwell.HtmlPlus.InputType.basefield.prototype.add = function(data) {
	var type = this.config.classType;
	var _4b6 = maxwell.HtmlPlus.Util.CanCast(data.type, this.config.datatype);
	var _4b7 = "dropdownitem";
	if (this.odd) {
		_4b7 += " odd";
	}
	if (!_4b6) {
		_4b7 += " disabled";
	}
	var n = cn("li", {
		title : data.type,
		datatype : data.type,
		className : _4b7,
		val : data.value,
		disabled : !_4b6
	}, {
		display : "block",
		position : "relative",
		width : "100%"
	}, "<span class='prefix'>" + data.prefix + "</span>" + data.name);
	this.list.appendChild(n);
	this.odd = !this.odd;
	return n;
};
maxwell.HtmlPlus.InputType.basefield.prototype.changeDataType = function(_4b9) {
	this.config.datatype = _4b9;
	var type = this.config.classType;
	var lis = this.list.getElementsByTagName("li");
	for ( var i = 0; i < lis.length; i++) {
		var li = lis[i];
		var _4be = maxwell.HtmlPlus.Util.CanCast(li.getAttribute("datatype"),
				_4b9);
		sn(li, {
			disabled : !_4be
		});
		if (_4be) {
			YAHOO.util.Dom.removeClass("disabled");
		} else {
			YAHOO.util.Dom.addClass("disabled");
		}
	}
};
$namespace("maxwell.HtmlPlus.InputType.editablefield");
maxwell.HtmlPlus.InputType.editablefield = function(_4bf, _4c0) {
	maxwell.HtmlPlus.InputType.editablefield.superclass.constructor.call(this,
			_4bf, _4c0);
};
$extend(maxwell.HtmlPlus.InputType.editablefield,
		maxwell.HtmlPlus.InputType.basefield);
maxwell.HtmlPlus.InputType.editablefield.prototype.getInputValue = function() {
	var val = maxwell.HtmlPlus.InputType.editablefield.superclass.getInputValue
			.call(this);
	if (this.selectedIndex == -1) {
		if (val.value !== this.config.fieldtype
				&& val.value.indexOf(this.config.fieldtype + ".") !== 0
				&& val.value != this.config.fieldtype) {
			return val;
		}
	}
	var val2 = {
		subkey : val.value,
		type : val.type
	};
	if (val2.subkey && val2.subkey.indexOf(this.config.fieldtype + ".") == 0) {
		val2.subkey = val2.subkey.substring(this.config.fieldtype.length + 1);
	} else {
		if (val2.subkey && val2.subkey == this.config.fieldtype) {
			val2.subkey = "";
		}
	}
	if (this.terminal.computedId != "_INPUT") {
		val2.terminal = this.terminal.computedId;
	}
	return val2;
};
maxwell.HtmlPlus.InputType.editablefield.prototype.setInputValue = function(
		_4c3) {
	if (_4c3 && _4c3.subkey) {
		_4c3.value = this.config.fieldtype;
		if (_4c3.subkey.length > 0) {
			_4c3.value = _4c3.value + "." + _4c3.subkey;
		}
	}
	maxwell.HtmlPlus.InputType.editablefield.superclass.setInputValue.call(
			this, _4c3);
};
$namespace("maxwell.HtmlPlus.InputType.field");
maxwell.HtmlPlus.InputType.field = function(_4c4, _4c5) {
	_4c5.editable = true;
	_4c5.notwirable = true;
	maxwell.HtmlPlus.InputType.field.superclass.constructor.call(this, _4c4,
			_4c5);
};
$extend(maxwell.HtmlPlus.InputType.field, maxwell.HtmlPlus.InputType.basefield);
maxwell.HtmlPlus.InputType.field.prototype.getInputValue = function() {
	var val = maxwell.HtmlPlus.InputType.field.superclass.getInputValue
			.call(this);
	if (val.value.indexOf(this.config.fieldtype) == 0) {
		val.value = val.value.substring(this.config.fieldtype.length + 1);
	}
	return val;
};
maxwell.HtmlPlus.InputType.field.prototype.setInputValue = function(_4c7) {
	if (_4c7 && _4c7.value && _4c7.value.length > 0
			&& _4c7.value != this.config.fieldtype) {
		_4c7.value = this.config.fieldtype + "." + _4c7.value;
	} else {
		if (_4c7 && _4c7.value && _4c7.value.length == 0) {
			_4c7.value = this.config.fieldtype;
		}
	}
	maxwell.HtmlPlus.InputType.field.superclass.setInputValue.call(this, _4c7);
};
$namespace("maxwell.HtmlPlus.InputType.fieldfilter");
maxwell.HtmlPlus.InputType.fieldfilter = function(_4c8, _4c9) {
	var html = "<input name=\"field\" type=\"field\">";
	html += "<select name=\"op\">";
	html += "<option value=\"contains\">Contains</option>";
	html += "<option value=\"doesnotcontain\">Does not contain</option>";
	html += "<option value=\"matches\">Matches regex</option>";
	html += "<option value=\"greater\">is greater than</option>";
	html += "<option value=\"is\">is</option>";
	html += "<option value=\"less\">is less than</option>";
	html += "</select>";
	html += "<input name=\"filter\" type=\"field\">";
	var span = cn("span", {}, {}, html);
	_4c8.parentNode.replaceChild(span, _4c8);
	this.expandedNode = span;
};
$namespace("maxwell.HtmlPlus.InputType.fieldopvalue");
maxwell.HtmlPlus.InputType.fieldopvalue = function(_4cc, _4cd) {
	var html = "<input name=\"field\" type=\"field\">";
	html += "<select name=\"op\">";
	html += "<option value=\"contains\">Contains</option>";
	html += "<option value=\"doesnotcontain\">Does not contain</option>";
	html += "<option value=\"matches\">Matches regex</option>";
	html += "<option value=\"greater\">is greater than</option>";
	html += "<option value=\"is\">is</option>";
	html += "<option value=\"less\">is less than</option>";
	html += "<option value=\"after\">is after</option>";
	html += "<option value=\"before\">is before</option>";
	html += "</select>";
	html += "<input name=\"value\" type=\"text\">";
	var span = cn("span", {}, {}, html);
	_4cc.parentNode.replaceChild(span, _4cc);
	this.expandedNode = span;
};
$namespace("maxwell.HtmlPlus.InputType.fieldrename");
maxwell.HtmlPlus.InputType.fieldrename = function(_4d0, _4d1) {
	var html = "<input name=\"field\" type=\"field\">";
	html += "<select name=\"op\">";
	html += "<option value=\"rename\">Rename</option>";
	html += "<option value=\"copy\">Copy As</option>";
	html += "</select>";
	html += "<input name=\"newval\" type=\"text\">";
	var span = cn("span", {}, {}, html);
	_4d0.parentNode.replaceChild(span, _4d0);
	this.expandedNode = span;
};
$namespace("maxwell.HtmlPlus.InputType.number");
maxwell.HtmlPlus.InputType.number = function(_4d4, _4d5) {
	_4d5.nocontainer = true;
	maxwell.HtmlPlus.InputType.number.superclass.constructor.call(this, _4d4,
			_4d5);
	maxwell.HtmlPlus.Util.manageInput(this.input, "number");
};
$extend(maxwell.HtmlPlus.InputType.number, maxwell.HtmlPlus.WirableInput);
$namespace("maxwell.HtmlPlus.InputType.url");
maxwell.HtmlPlus.InputType.url = function(_4d6, _4d7) {
	this.onurlinfo = new YAHOO.util.CustomEvent("onurlinfo", this, true);
	_4d7.nocontainer = true;
	maxwell.HtmlPlus.InputType.url.superclass.constructor
			.call(this, _4d6, _4d7);
	this.icon = cn("img", {
		className : "favico",
		width : "16px",
		height : "16px",
		src : "http://l.yimg.com/a/i/tb/iconsgif/blank.gif"
	});
	maxwell.HtmlPlus.Util.manageInput(_4d6, "url");
	YAHOO.util.Event
			.addListener(_4d6, "keypress", this.checkchange, this, true);
	YAHOO.util.Event.addListener(_4d6, "blur", this.checkchange, this, true);
	this.timer = true;
	this.lasturl = null;
	_4d6.parentNode.insertBefore(this.icon, _4d6);
};
$extend(maxwell.HtmlPlus.InputType.url, maxwell.HtmlPlus.WirableInput);
maxwell.HtmlPlus.InputType.url.prototype.checkchange = function(e) {
	if (this.timer) {
		window.clearTimeout(this.timer);
		this.timer = null;
	}
	if (this.lasturl && this.lasturl == this.input.value) {
		return;
	}
	var self = this;
	this.timer = window.setTimeout(function() {
		self.timer = null;
		self.change(self.input.value);
	}, 1000);
};
maxwell.HtmlPlus.InputType.url.prototype.change = function(url) {
	this.lasturl = url;
	if (url.length > 0 && !this.disabled) {
		var self = this;
		ajaxCall("feed/preview", {
			"_out" : "json",
			"url" : url
		}, function(data) {
			self.onurlinfo.fire(data);
			self.goodurl(data);
		}, function(err) {
			self.badurl(err);
		});
	} else {
		sn(this.icon, {
			src : "http://l.yimg.com/a/i/tb/iconsgif/blank.gif"
		});
	}
};
maxwell.HtmlPlus.InputType.url.prototype.badurl = function(_4de) {
	this.input.setAttribute("title", "Unable to resolve URL " + _4de);
	this.icon
			.setAttribute("src",
					"http://l.yimg.com/a/i/us/fi/yfc/images/error_images/warning_1.1.gif");
};
maxwell.HtmlPlus.InputType.url.prototype.goodurl = function(data) {
	this.icon.setAttribute("src", data.result.favicon);
};
$namespace("maxwell.HtmlPlus.InputType.datetime");
maxwell.HtmlPlus.InputType.datetime = function(_4e0, _4e1) {
	maxwell.HtmlPlus.InputType.datetime.superclass.constructor.call(this, _4e0,
			_4e1);
	maxwell.HtmlPlus.Util.manageInput(this.input, "date/time");
};
$extend(maxwell.HtmlPlus.InputType.datetime, maxwell.HtmlPlus.WirableInput);
maxwell.HtmlPlus.InputType.datetime.prototype.beforeOpen = function(_4e2) {
	if (!this.cal) {
		var id = $id(this.container);
		this.cal = new YAHOO.widget.Calendar("cal" + id, id);
		this.cal.selectEvent.subscribe(this.handleSelect, this, true);
		var self = this;
		this.cal.doCellMouseOut = function(e, cal) {
			self.overContainer = false;
		};
		this.cal.doCellMouseOver = function(e, cal) {
			self.overContainer = true;
		};
		this.cal.doSelectCell = function(e, cal) {
			YAHOO.widget.Calendar.prototype.doSelectCell(e, cal);
			YAHOO.util.Event.stopEvent(e);
		};
		this.cal.render();
		this.w = this.container.offsetWidth - 4;
		if (this.w < _4e2) {
			this.w = _4e2;
		}
	}
	this.container.style.width = this.w + "px";
};
maxwell.HtmlPlus.InputType.datetime.prototype.handleSelect = function(type,
		args, obj) {
	var _4ee = args[0];
	var date = _4ee[0];
	var year = date[0], _4f1 = date[1], day = date[2];
	this.setInputValue({
		value : _4f1 + "/" + day + "/" + year
	});
	this.close();
	this.hasFocus = false;
	this.onchangefocus.fire(this.hasFocus);
};
$namespace("maxwell.HtmlPlus.InputType.location");
maxwell.HtmlPlus.InputType.location = function(_4f3, _4f4) {
	_4f4.nocontainer = true;
	maxwell.HtmlPlus.InputType.location.superclass.constructor.call(this, _4f3,
			_4f4);
	YAHOO.util.Dom.addClass(this.input, "locationinput");
	maxwell.HtmlPlus.Util.manageInput(this.input, "location");
};
$extend(maxwell.HtmlPlus.InputType.location, maxwell.HtmlPlus.WirableInput);
$namespace("maxwell.HtmlPlus.InputType.module");
maxwell.HtmlPlus.InputType.module = function(_4f5, _4f6) {
	_4f6.nocontainer = true;
	_4f6.notwirable = true;
	maxwell.HtmlPlus.InputType.module.superclass.constructor.call(this, _4f5,
			_4f6);
	this.onsetmodule = new YAHOO.util.CustomEvent("onsetmodule", this, true);
	this.onclearmodule = new YAHOO.util.CustomEvent("onclearmodule", this, true);
	this.input.style.display = "none";
	this.moduleholder = cn("div", {
		className : "moduleholder"
	}, null, this.inviteText);
	maxwell.Util.insertAfter(this.moduleholder, this.input);
	maxwell.Util.insertAfter(cn("br"), this.input);
	var _4f7 = [ "system:sources", "rssmodule", "system:my pipes" ];
	if (_4f5.getAttribute("moduletype") == "all") {
		_4f7 = [ "system:sources", "rssmodule", "system:my pipes",
				"system:favorites", "system:item", "system:date",
				"system:number", "system:location", "system:string",
				"system:url" ];
	}
	var dd = new maxwell.DragDrop.Target($id(this.moduleholder), _4f7);
	dd.onSourceDragDrop.subscribe(function(type, args, me) {
		var _4fc = args[0];
		var _4fd = null;
		if (_4fc.hasDataType("moduletype")) {
			var _4fe = args[0].getData();
			this.addSubModule(_4fe);
		} else {
			var _4ff = _4fc.getData();
			if (_4ff.source == "pipe") {
				this.addSubModule(_4ff.type);
			} else {
				var conf = {
					"URL" : {
						type : "url",
						value : _4ff.url
					}
				};
				this.addSubModule("fetch", conf);
			}
		}
	}, this, true);
	dd.startSourceDrag.subscribe(function(type, args, me) {
		this.moduleholder.style.border = "2px dashed #400000";
	}, this, true);
	dd.onSourceDragEnter.subscribe(function(type, args, me) {
		this.moduleholder.style.border = "2px solid red";
	}, this, true);
	dd.onSourceDragOut.subscribe(function(type, args, me) {
		this.moduleholder.style.border = "2px dashed #400000";
	}, this, true);
	dd.endSourceDrag.subscribe(function(type, args, me) {
		this.moduleholder.style.border = "0px";
	}, this, true);
	this.dd = dd;
};
$extend(maxwell.HtmlPlus.InputType.module, maxwell.HtmlPlus.WirableInput);
maxwell.HtmlPlus.InputType.module.prototype.inviteText = "<div class='dropinvitation'>Drop module/pipe from toolbox here<span>(no user inputs, operators or deprecated modules)</span></div>";
maxwell.HtmlPlus.InputType.module.prototype.clearModule = function() {
	if (this.m) {
		this.m.remove();
		this.moduleholder.innerHTML = this.inviteText;
		this.onclearmodule.fire(this, this.m);
		this.m = null;
		this.module.resize(this.originalWidth);
		this.module.fireOnMove();
	}
};
maxwell.HtmlPlus.InputType.module.prototype.initSubwalker = function(type,
		args, self) {
	this.m.walker.createCustomInput = function(node) {
		var _511 = {
			module : self.module,
			editable : true
		};
		var inp = maxwell.HtmlPlus.CreateEx("editablefield", node, _511);
		if (inp.internalTerminal) {
			self.m.addTerminal(inp.internalTerminal);
		}
		return inp;
	};
};
maxwell.HtmlPlus.InputType.module.prototype.addSubModule = function(type, conf,
		id) {
	if (this.m) {
		this.clearModule();
	}
	if (!this.module) {
		this.module = maxwell.Module.getModule(this.node);
		if (this.module) {
			this.module.onselect.subscribe(this._select, this, true);
			this.module.onunselect.subscribe(this._unselect, this, true);
			this.module.onmove.subscribe(this._move, this, true);
			this.module.onremove.subscribe(this._remove, this, true);
		}
	}
	var m = maxwell.Module.Create(type, {
		conf : conf,
		isSubContent : true,
		id : id,
		style : {
			visibility : "hidden"
		}
	});
	document.body.appendChild(m.node);
	this.m = m;
	if (m.dd) {
		m.dd.unreg();
	}
	m.onready.subscribe(function() {
		if (!this.originalWidth) {
			this.originalWidth = this.module.hd.offsetWidth;
		}
		if (this.originalWidth > m.hd.offsetWidth) {
			m.resize(this.originalWidth - 16);
		} else {
			this.module.resize(m.hd.offsetWidth + 16);
		}
		sn(m.node, {
			submodule : "true"
		});
		YAHOO.util.Dom.replaceClass(m.node, "mod", "innermod");
		this.moduleholder.innerHTML = "";
		this.moduleholder.appendChild(m.node);
		YAHOO.util.Event.onAvailable($id(m.node), function() {
			this.onsetmodule.fire(this, m);
		}, this, true);
	}, this, true);
	m.onbeforewalk.subscribe(this.initSubwalker, this, true);
	m.onremove.subscribe(this.clearModule, this, true);
	m.hide.parentNode.removeChild(m.hide);
	this.moduleholder.innerHTML = "&nbsp;<i>Creating module...</i>";
};
maxwell.HtmlPlus.InputType.module.prototype._move = function() {
	if (this.m) {
		this.m.fireOnMove();
	}
};
maxwell.HtmlPlus.InputType.module.prototype._remove = function() {
	if (this.m) {
		this.m.remove();
	}
};
maxwell.HtmlPlus.InputType.module.prototype._select = function() {
	if (this.m) {
		this.m.select();
	}
};
maxwell.HtmlPlus.InputType.module.prototype._unselect = function() {
	if (this.m) {
		this.m.unselect();
	}
};
maxwell.HtmlPlus.InputType.module.prototype.getValue = function() {
	var val = {};
	if (this.m) {
		val.value = this.m.freeze();
		val.type = "module";
	}
	return val;
};
maxwell.HtmlPlus.InputType.module.prototype.setValue = function(val) {
	var _519 = val.value;
	if (_519) {
		this.addSubModule(_519.type, _519.conf, _519.id);
		return true;
	}
	return false;
};
$namespace("maxwell.HtmlPlus.InputType.text");
maxwell.HtmlPlus.InputType.text = function(_51a, _51b) {
	_51b.nocontainer = true;
	maxwell.HtmlPlus.InputType.text.superclass.constructor.call(this, _51a,
			_51b);
	maxwell.HtmlPlus.Util.manageInput(this.input, "text");
};
$extend(maxwell.HtmlPlus.InputType.text, maxwell.HtmlPlus.WirableInput);
$namespace("maxwell.HtmlPlus.InputType.privat");
maxwell.HtmlPlus.InputType._private = function(_51c, _51d) {
	_51d.nocontainer = true;
	maxwell.HtmlPlus.InputType._private.superclass.constructor.call(this, _51c,
			_51d);
	maxwell.HtmlPlus.Util.manageInput(this.input, "text");
};
$extend(maxwell.HtmlPlus.InputType._private, maxwell.HtmlPlus.WirableInput);
$namespace("maxwell.RSSDebugger");
maxwell.RSSDebugger = function(_51e) {
	this.editor = _51e;
	this.node = cn("div", {
		className : "rsspreview"
	}, {
		height : "200px"
	});
	this.title = cn("div", {
		className : "rsspreviewtitle"
	});
	this.refreshingImage = cn("div", {
		className : "refreshing"
	}, {
		display : "none"
	});
	this.tab = cn(
			"div",
			{
				className : "debugtabmain"
			},
			null,
			"<ul class='itemlist'></ul><span class='debugdebug'>Debugger:</span><span class='itemtitle'><i>none</i></span><span class='itemcount' style='display:none'></span><div class='debugtableft'></div><div class='debugtabright'>");
	var _51f = this.tab.getElementsByTagName("span");
	this.itemCount = _51f[2];
	this.itemTitle = _51f[1];
	this.select = this.tab.getElementsByTagName("ul")[0];
	var tmp = cn("span");
	tmp.innerHTML = "<table cellpadding=0 cellspacing=0 class='rsstable'><tbody class='rsstbody'><tr class='rsstr'><td class='rsstd'></td></tr></tbody></table>";
	this.node.appendChild(this.refreshingImage);
	this.node.appendChild(this.title);
	this.errors = cn("ul", {
		className : "errorlist"
	}, {
		display : "none"
	});
	this.node.appendChild(this.errors);
	this.node.appendChild(tmp);
	this.treeNode = tmp.getElementsByTagName("td")[0];
	this.buildTitle();
	_51e.layout.onnewblock.subscribe(this.onnewblock, this, true);
	_51e.layout.onremoveblock.subscribe(this.onremoveblock, this, true);
	YAHOO.util.Event.addListener(this.tab, "mouseover", function(e) {
		if (this.hideselect) {
			window.clearTimeout(this.hideselect);
			this.hideselect = null;
		}
		if (!this.anim) {
			this.select.style.left = "-5px";
			this.select.style.visibility = "hidden";
			this.select.style.display = "block";
			this.select.style.height = "auto";
			this.select.style.width = (this.tab.offsetWidth + 7) + "px";
			var h = this.select.offsetHeight - 2;
			this.select.style.height = "0px";
			this.select.style.bottom = (this.tab.offsetHeight - 5) + "px";
			this.select.style.visibility = "visible";
			this.anim = new YAHOO.util.Anim(this.select, {
				height : {
					to : h
				}
			}, 0.2, YAHOO.util.Easing.easeOut);
			this.anim.animate();
		}
	}, this, true);
	YAHOO.util.Event.addListener(this.select, "mouseout", function() {
		var self = this;
		if (!this.hideselect) {
			this.hideselect = window.setTimeout(function() {
				self.select.style.display = "none";
				self.hideselect = null;
				self.anim = null;
			}, 100);
		}
	}, this, true);
	YAHOO.util.Event.addListener(this.tab, "mouseout", function() {
		var self = this;
		if (!this.hideselect) {
			this.hideselect = window.setTimeout(function() {
				self.select.style.display = "none";
				self.hideselect = null;
				self.anim = null;
			}, 100);
		}
	}, this, true);
	YAHOO.util.Event.addListener(this.select, "mouseover", function() {
		if (this.hideselect) {
			window.clearTimeout(this.hideselect);
			this.hideselect = null;
		}
	}, this, true);
	YAHOO.util.Event.addListener(this.select, "click", function(e) {
		var li = YAHOO.util.Event.getTarget(e);
		if (li == this.select) {
			return;
		}
		while (li && li.tagName != "LI" && li != this.select) {
			li = li.parentNode;
		}
		this.select.style.display = "none";
		if (!(li && li.tagName == "LI") || li == this.select.childNodes[0]) {
			return;
		}
		var _527 = li.getAttribute("mid");
		var m = this.editor.getModuleById(_527);
		m.select();
	}, this, true);
};
maxwell.RSSDebugger.prototype.setSplitter = function(_529) {
	_529.node.appendChild(this.tab);
	YAHOO.util.Event.onAvailable($id(this.tab), function(self) {
		self.tabshim = maxwell.Util.addShim(self.tab);
	}, this, true);
};
maxwell.RSSDebugger.prototype.onnewblock = function(type, args, self) {
	var _52e = args[0];
	_52e.onready.subscribe(function() {
		var li = cn("li", {
			mid : _52e.id
		}, null, _52e.def.name);
		this.select.appendChild(li);
	}, this, true);
};
maxwell.RSSDebugger.prototype.onremoveblock = function(type, args, self) {
	var _533 = args[0];
	var _534 = this.select.getElementsByTagName("li");
	for ( var i = 0; i < _534.length; i++) {
		if (_534[i].getAttribute("mid") == _533.id) {
			this.select.removeChild(_534[i]);
			return;
		}
	}
};
maxwell.RSSDebugger.prototype.onopentab = function(type, args, self) {
	if (args[0] == this.tabIndex) {
		if (!this.module) {
			try {
				this
						.setSourceModule(this.editor.getModuleByType("output").terminals[0].wires[0].src.block);
			} catch (ex) {
			}
		}
	}
};
maxwell.RSSDebugger.prototype.buildTitle = function() {
	this.timeTaken = cn("span", {
		className : "timetaken"
	}, {
		display : "none"
	});
	this.refreshButton = cn("span", {
		className : "refresh"
	}, {
		display : "none"
	}, "Refresh");
	this.title.appendChild(this.timeTaken);
	this.title.appendChild(this.refreshButton);
	YAHOO.util.Event.addListener(this.refreshButton, "click", this.refresh,
			this, true);
	this.tree = new maxwell.SimpleTree();
	this.treeNode.appendChild(this.tree.node);
	this.tree.onbranchopen.subscribe(this.renderBranch, this, true);
};
maxwell.RSSDebugger.prototype.setTimeTaken = function(time) {
	if (time) {
		this.timeTaken.innerHTML = "Time taken: <span>" + time + "</span>s";
	} else {
		this.timeTaken.innerHTML = "Preview failed";
	}
	this.timeTaken.style.display = "inline";
};
maxwell.RSSDebugger.prototype.setItemCount = function(_53a) {
	if (!_53a) {
		_53a = 0;
	}
	var t = "(<span>" + _53a + "</span> item";
	if (_53a != 1) {
		t += "s)";
	} else {
		t += ")";
	}
	this.itemCount.innerHTML = t;
	this.itemCount.style.display = "inline";
	this.tabshim.rsize(this.tabshim);
};
maxwell.RSSDebugger.prototype.refresh = function() {
	this.editor.preview();
};
maxwell.RSSDebugger.prototype.setSourceModule = function(_53c, _53d) {
	if (this.module) {
		this.module.onpreviewstart.unsubscribe(this.previewstart, this, true);
		this.module.onpreviewavailable.unsubscribe(this.previewavailable, this,
				true);
	}
	this.module = _53c;
	this.itemTitle.innerHTML = _53c.def.name;
	this.tabshim.rsize(this.tabshim);
	this.module.onpreviewstart.subscribe(this.previewstart, this, true);
	this.module.onpreviewavailable.subscribe(this.previewavailable, this, true);
	if ((_53c.previewData || _53c.errorData) && !_53c.dirty && !_53d) {
		this.tree.clear();
		this.render(_53c.previewData, _53c.errorData);
	} else {
		this.refresh();
	}
};
maxwell.RSSDebugger.prototype.previewstart = function(type, args, self) {
	this.timeTaken.style.display = "none";
	this.itemCount.style.display = "none";
	this.refreshButton.style.display = "none";
	this.errors.style.display = "none";
	this.refreshingImage.style.display = "block";
	this.tree.clear();
};
maxwell.RSSDebugger.prototype.previewavailable = function(type, args, self) {
	this.refreshingImage.style.display = "none";
	this.render(args[0], args[1]);
};
maxwell.RSSDebugger.prototype.isHTML = function(data) {
	var type = typeof (data);
	if (type == "string"
			&& (data.indexOf("<br") >= 0 || data.indexOf("<a") >= 0
					|| data.indexOf("<img") >= 0 || data.indexOf("<p") >= 0 || data
					.indexOf("<div") > 0)) {
		return true;
	}
	return false;
};
maxwell.RSSDebugger.prototype.isLargeText = function(data) {
	var type = typeof (data);
	if (type == "string" && data.length > 100) {
		return true;
	}
	return false;
};
maxwell.RSSDebugger.prototype.render = function(rss, _549) {
	this.count = 0;
	if (rss) {
		this.constructBranch(this.tree.root, rss.items);
		if (rss.items && rss.items.length < rss.item_count) {
			this.tree.addNode(this.tree.root, this.tree.createNode("<p>+"
					+ (rss.item_count - rss.items.length) + " more...</p>"));
		}
		this.setTimeTaken(rss.duration);
		this.setItemCount(rss.item_count);
	} else {
		this.setTimeTaken(null);
		this.setItemCount(0);
	}
	this.renderErrors(_549);
	this.refreshButton.style.display = "inline";
};
maxwell.RSSDebugger.prototype.renderBranch = function(type, args, me) {
	if (args[0].childNodes.length > 0) {
		return;
	}
	var keys = this.tree.getKeyChain(args[0]);
	var o = this.module.previewData.items;
	for ( var i = keys.length - 1; i >= 0; i--) {
		o = o[keys[i]];
	}
	this.constructBranch(args[0], o);
};
maxwell.RSSDebugger.prototype.toggleHtmlView = function(a1, a2, d1, d2, e) {
	YAHOO.util.Dom.addClass(a1, "selected");
	YAHOO.util.Dom.removeClass(a2, "selected");
	YAHOO.util.Dom.removeClass(d1, "unselected");
	YAHOO.util.Dom.addClass(d2, "unselected");
	YAHOO.util.Event.stopEvent(e);
};
maxwell.RSSDebugger.prototype.constructBranch = function(root, data) {
	var type = typeof (data);
	if (type == "object" && data) {
		for ( var key in data) {
			if (!data[key]) {
				continue;
			}
			if (this.tree.root == root && (data[key].title || data[key].Title)) {
				var _559 = data[key].title;
				if (!_559) {
					_559 = data[key].Title;
				}
				if (!_559) {
					_559 = data[key].value;
				}
				if (!_559 || typeof (_559) != "string") {
					_559 = key;
				}
				_559 = _559.replace(/&quot;/g, "\"");
				_559 = _559.replace(/&#39;/g, "'");
				var node = this.tree.createNode(cook(_559), key, true);
				this.tree.addNode(root, node);
				this.count++;
				continue;
			}
			var _55b = typeof (data[key]);
			var _55c = data[key];
			var _55d = this.isHTML(_55c);
			var _55e = this.isLargeText(_55c);
			var _55f = (!_55c || (!(_55b == "object" || _55d || _55e)));
			if (_55f) {
				key = key.replace(/&quot;/g, "\"");
				key = key.replace(/&#39;/g, "'");
				var txt = "<span class='name'>" + cook(key)
						+ "</span><span class='value " + typeof (_55c) + "'>"
						+ cook(_55c) + "</span>";
				var n = this.tree.createNode(txt);
				YAHOO.util.Dom.addClass(n, "nv");
				this.tree.addNode(root, n);
				continue;
			}
			var node = this.tree.createNode(key, key, true);
			this.tree.addNode(root, node);
			continue;
		}
	} else {
		var node = null;
		if (this.isHTML(data)) {
			var _562 = this.htmlEncode(data, true, 0);
			var _563 = "<span class='htmlsubkey'>[<a class='selected'>html</a>|<a>source</a>]</span>";
			_563 += "<div class=\"htmlholder\"></div>";
			_563 += "<div class=\"htmlholder source unselected\"></div>";
			node = this.tree.createNode(_563);
			this.tree.addNode(root, node);
			var self = this;
			window.setTimeout(function() {
				var a = node.getElementsByTagName("a");
				var d = node.getElementsByTagName("div");
				var d1 = d[0];
				var d2 = d[1];
				d1.innerHTML = data;
				d2.innerHTML = _562;
				YAHOO.util.Event.addListener(a[0], "click", function(e) {
					this.toggleHtmlView(a[0], a[1], d1, d2, e);
				}, self, true);
				YAHOO.util.Event.addListener(a[1], "click", function(e) {
					this.toggleHtmlView(a[1], a[0], d2, d1, e);
				}, self, true);
			}, 100);
		} else {
			if (this.isLargeText(data)) {
				var _563 = "<div class=\"textholder\">"
						+ this.htmlEncode(data, true, 0) + "</div>";
				node = this.tree.createNode(_563);
				this.tree.addNode(root, node);
			} else {
				node = this.tree.createNode(data);
				this.tree.addNode(root, node);
			}
		}
	}
};
maxwell.RSSDebugger.prototype.renderErrors = function(_56b) {
	if (_56b && _56b.length > 0) {
		this.errors.style.display = "block";
	} else {
		this.errors.style.display = "none";
		return;
	}
	this.errors.innerHTML = null;
	for ( var i in _56b) {
		var err = _56b[i];
		if (!err) {
			continue;
		}
		if (!err.message || !err.type) {
			continue;
		}
		var html = "<img src='http://l.yimg.com/a/i/space.gif' /><span>"
				+ err.message + "</span>";
		var li = cn("li", {
			className : "error " + err.type
		}, {}, html);
		this.errors.appendChild(li);
	}
};
maxwell.RSSDebugger.prototype.htmlEncode = function(_570, _571, tabs) {
	function special(_573) {
		var _574 = "";
		for ( var i = 0; i < _573.length; i++) {
			var c = _573.charAt(i);
			if (c < " " || c > "~") {
				c = "&#" + c.charCodeAt() + ";";
			}
			_574 += c;
		}
		return _574;
	}
	function format(_577) {
		tabs = (tabs >= 0) ? Math.floor(tabs) : 4;
		var _578 = _577.split(/\r\n|\r|\n/);
		for ( var i = 0; i < _578.length; i++) {
			var line = _578[i];
			var _57b = "";
			for ( var p = 0; p < line.length; p++) {
				var c = line.charAt(p);
				if (c === "\t") {
					var _57e = tabs - (_57b.length % tabs);
					for ( var s = 0; s < _57e; s++) {
						_57b += " ";
					}
				} else {
					_57b += c;
				}
			}
			_57b = _57b.replace(/(^ )|( $)/g, "&nbsp;");
			_578[i] = _57b;
		}
		var _580 = _578.join("<br />");
		_580 = _580.replace(/  /g, " &nbsp;");
		return _580;
	}
	var _581 = _570;
	_581 = _581.replace(/\&/g, "&amp;");
	_581 = _581.replace(/\</g, "&lt;");
	_581 = _581.replace(/\>/g, "&gt;");
	if (_571) {
		_581 = format(_581);
	} else {
		_581 = _581.replace(new RegExp("\"", "g"), "&quot;");
	}
	_581 = special(_581);
	return _581;
};
$namespace("maxwell.ModuleLibrary");
maxwell.ModuleLibrary = function() {
	this.modules = [];
	this.inputs = [];
	this.onnewlibrarymodule = new YAHOO.util.CustomEvent("onnewlibrarymodule",
			this);
	this.node = cn("div", {
		className : "toolbox"
	});
	this.librarynode = cn("div", {
		className : "librarymodules"
	});
	this.helpnode = cn("div", {
		className : "helparea"
	}, null, "<div class='helpcontainer'></div>");
	this.helpcontainer = this.helpnode.firstChild;
	this.accordion = new maxwell.Accordion($id(this.librarynode), [],
			maxwell.Accordion.LAYOUT_VERT);
	this.node.appendChild(this.librarynode);
	this.node.appendChild(this.helpnode);
	this.splitter = new maxwell.Splitter.TopBottom(this.librarynode,
			this.helpnode, "smalltopbottom");
	this.accordion.onInit.subscribe(this.retrieveModules, this, true);
	this.accordion.onBeforeOpen.subscribe(function(type, args, me) {
		var _585 = args[0];
		var node = this.accordion.section_list[_585].title;
		YAHOO.util.Dom.removeClass(node, "show");
		YAHOO.util.Dom.addClass(node, "hide");
	}, this, true);
	this.accordion.onBeforeClose.subscribe(function(type, args, me) {
		var _58a = args[0];
		var node = this.accordion.section_list[_58a].title;
		YAHOO.util.Dom.removeClass(node, "hide");
		YAHOO.util.Dom.addClass(node, "show");
	}, this, true);
};
maxwell.ModuleLibrary.categories = [ "Image Sources", "Plugins", "Favorites" ];
maxwell.ModuleLibrary.prototype.retrieveModules = function() {
	var self = this;
	this.splitter.resizeAB();
	for ( var i = 0; i < maxwell.ModuleLibrary.categories.length; i++) {
		this.accordion.appendSection(maxwell.ModuleLibrary.categories[i]);
	}
	ajaxCall(
			"module/list",
			{
				"_out" : "json"
			},
			function(_58e) {
				_58e = _58e.module;
				for ( var i in _58e) {
					self.addModule(_58e[i]);
				}
				var _590 = self.accordion.findSectionByTitle("My pipes");
				var _591 = self.accordion.getSection(_590);
				var _592 = (_591.content.childNodes.length > 0);
				var _593 = {
					tags : [ "system:My pipes" ],
					name : "Insert new pipe",
					description : "Add a new sub-pipe into this pipe, and start editing it",
					type : "pipe:new"
				};
				self.addModule(_593, true);
				self.accordion.openSection(0);
				if (!_592 && is_new_editor) {
					ajaxCall("module/featured", {
						"_out" : "json"
					}, function(data) {
						new maxwell.Dialog.Draggable({
							content : "Welcome to the Pipes editor",
							body : "<div class='module_featured'>"
									+ data.description + "</div>"
						});
					}, function(_595) {
					});
				}
			}, function(_596) {
				new maxwell.Dialog.Alert({
					content : "System Error",
					title : _596
				});
			});
};
maxwell.ModuleLibrary.createLibraryModule = function(name, _598, _599) {
	var m = cn(
			"div",
			{
				className : "librarymodule " + _599
			},
			null,
			"<div class='librarymodulecontent'>"
					+ name
					+ "</div><div class='librarymoduleleft'></div><div class='librarymoduleright'></div><div class='librarymodulefade'></div>");
	m.setAttribute("title", _598);
	return m;
};
maxwell.ModuleLibrary.prototype.addModule = function(_59b, _59c) {
	this.modules[_59b.type] = _59b;
	var _59d = "other";
	if (!_59b.tags) {
		return;
	}
	if (this.isDeprecated(_59b)) {
		_59d = "Deprecated";
	} else {
		for ( var i = 0; i < _59b.tags.length; i++) {
			if (_59b.tags[i].indexOf("system:") == 0) {
				_59d = _59b.tags[i].substring(7, 8).toUpperCase()
						+ _59b.tags[i].substring(8);
				break;
			}
		}
	}
	var _59f = _59d.replace(/\s+/g, "");
	var m = maxwell.ModuleLibrary.createLibraryModule(_59b.name,
			_59b.description, _59f);
	YAHOO.util.Event.addListener(m, "click", function() {
		this.showHelp(_59b.type);
	}, this, true);
	var _5a1 = this.accordion.findSectionByTitle(_59d);
	if (_5a1 == null) {
		var _5a2 = this.accordion.appendSection(_59d, m);
		YAHOO.util.Dom.addClass(_5a2.title, "show");
	} else {
		var _5a2 = this.accordion.getSection(_5a1);
		var node = _5a2.content;
		var _5a4 = (node.childNodes.length == 0);
		if (_5a4) {
			YAHOO.util.Dom.addClass(_5a2.title, "show");
		}
		if (!_59c || _5a4) {
			node.appendChild(m);
		} else {
			node.insertBefore(m, node.firstChild);
		}
	}
	_59b.node = m;
	this.onnewlibrarymodule.fire(_59b);
};
maxwell.ModuleLibrary.prototype.help = {};
maxwell.ModuleLibrary.prototype.showHelp = function(type, _5a6) {
	if (this.help[type]) {
		this.setHelpText(this.help[type], _5a6);
		return;
	}
	var self = this;
	ajaxCall("module/help", {
		"type" : type,
		"_out" : "json"
	}, function(data) {
		self.help[data.type] = data.help;
		self.setHelpText(data.help, _5a6);
	}, function(_5a9) {
		this.setHelpText("", _5a6);
	});
};
maxwell.ModuleLibrary.prototype.setHelpText = function(txt, _5ab) {
	this.helpcontainer.innerHTML = txt;
	if (_5ab) {
		var _5ac = false;
		if (this.splitter.nodeB.offsetHeight < 150) {
			this.splitter.height = 150;
			this.splitter.animateShow();
			_5ac = true;
		}
		if (!_5ac) {
			var _5ad = YAHOO.util.Dom.getStyle(this.splitter.nodeB,
					"backgroundColor");
			var anim = new YAHOO.util.ColorAnim(this.splitter.nodeB, {
				backgroundColor : {
					to : "rgb(255, 255, 100)"
				}
			}, 0.3);
			var _5af = new YAHOO.util.ColorAnim(this.splitter.nodeB, {
				backgroundColor : {
					to : _5ad
				}
			}, 0.3);
			anim.onComplete.subscribe(function() {
				_5af.animate();
			}, this, true);
			anim.animate();
		}
	}
};
maxwell.ModuleLibrary.prototype.isDeprecated = function(_5b0) {
	for ( var i = 0; i < _5b0.tags.length; i++) {
		if (_5b0.tags[i] == ("system:deprecated")) {
			return true;
		}
	}
	return false;
};
$namespace("maxwell.PropertiesDialog");
maxwell.PropertiesDialog = function(_5b2) {
	var html = "<table cellspacing=0 cellpadding=0><tbody>";
	html += "<tr><td><label>Name</label></td></tr>";
	html += "<tr class='pipename'><td><input type=text></td></tr>";
	html += "<tr><td><label>Description</label></td></tr>";
	html += "<tr class='pipedesc'><td><textarea rows=5 cols=30></textarea></td></tr>";
	html += "</tbody></table>";
	var _5b4 = "<table cellspacing=0 cellpadding=0><tbody>";
	_5b4 += "<tr><td><label>Tags</label></td></tr>";
	_5b4 += "<tr class='pipetags'><td></td></tr>";
	if (_5b2.pipe.id) {
		var pid = _5b2.pipe.id;
		var _5b6 = "/pipes/pipe.info?_id=" + pid;
		var _5b7 = "_run" + pid;
		var a = "<a class='runpipe' target='+runid+' href='" + _5b6
				+ "'>Run Pipe...</a>";
		_5b4 += "<tr  style='display:block;position:relative;padding-top:8px'><td>"
				+ a + "</td></tr>";
		if (_5b2.dirty) {
			_5b4 += "<tr style='display:block;position:relative;padding-top:4px'><td><span>Note: this Pipe's changes are unsaved. The results displayed on the Pipe's run page may differ</span></td></tr>";
		}
	}
	_5b4 += "</tbody></table>";
	var self = this;
	var _5ba = new maxwell.Dialog.OkCancel({
		className : "pipeinfotab",
		canceltext : "Cancel",
		oktext : "OK",
		body : _5b4,
		content : html,
		onOk : function() {
			self.ok(false);
		}
	});
	var tds = _5ba.node.getElementsByTagName("TD");
	var ta = _5ba.node.getElementsByTagName("TEXTAREA");
	this.description = ta[0];
	this.tags = new maxwell.Taglist([]);
	tds[5].appendChild(this.tags.node);
	this.name = tds[1].getElementsByTagName("INPUT")[0];
	this.updateInfo(_5b2.pipe);
	this.editor = _5b2;
};
maxwell.PropertiesDialog.prototype.updateInfo = function(info) {
	this.tags.clear();
	if (info.tags) {
		this.tags.addTags(info.tags);
	}
	if (info.description) {
		this.description.value = info.description;
	} else {
		this.description.value = "";
	}
	if (info.name) {
		this.name.value = info.name;
	} else {
		this.name.value = "";
	}
};
maxwell.PropertiesDialog.prototype.ok = function(_5be) {
	this.editor.pipe.name = this.name.value;
	this.editor.pipe.description = this.description.value;
	this.editor.pipe.tags = this.tags.getTags();
	if (_5be) {
		this.editor.pipe.publish = true;
		this.editor.save();
	} else {
		maxwell.PipesEditor._instance.tabs.onchangetitle.fire(this.editor,
				this.name.value);
	}
};
$namespace("maxwell.SearchDropDown");
maxwell.SearchDropDown = function() {
	this.modules = [];
	this.inputs = [];
	this.node = cn(
			"div",
			{
				className : "searchholder"
			},
			null,
			"<div class='searchbar'><input type='text' autocomplete='off'/><div class='search'></div></div><div class='searchMatches'><ul></ul><div class='searchStatus'><span></span></div></div>");
	this.results = this.node.getElementsByTagName("ul")[0];
	this.searchresults = this.results.parentNode;
	this.status = this.searchresults.lastChild;
	var _5bf = maxwell.Toolbar.createSmallButton("Close").firstChild;
	YAHOO.util.Dom.addClass(_5bf, "searchclose");
	this.status.appendChild(_5bf);
	this.inputBox = this.node.getElementsByTagName("input")[0];
	this.progress = cn("div", {
		className : "refreshing"
	}, {
		display : "none",
		top : "-12px"
	});
	this.status.appendChild(this.progress);
	this.searchresults.style.display = "none";
	maxwell.HtmlPlus.Util.manageInput(this.inputBox,
			"Search for pipes and feeds");
	YAHOO.util.Event.addListener(this.inputBox.nextSibling, "click",
			this.doSearch, this, true);
	YAHOO.util.Event.addListener(this.inputBox, "focus", this.open, this, true);
	YAHOO.util.Event.addListener(this.inputBox, "keydown", this.clear, this,
			true);
	YAHOO.util.Event.addListener(_5bf, "click", this.close, this, true);
	this.onerror = new YAHOO.util.CustomEvent("onerror", this, true);
	this.onstart = new YAHOO.util.CustomEvent("onstart", this, true);
	this.onfinish = new YAHOO.util.CustomEvent("onfinish", this, true);
	this.onstart.subscribe(function(type, args, self) {
		this.setStatus("Searching...");
		this.progress.style.display = "block";
		this.inputBox.setAttribute("disabled", "disabled");
	}, this, true);
	this.onfinish.subscribe(function(type, args, self) {
		this.results.parentNode.style.display = "block";
		this.progress.style.display = "none";
		this.inputBox.removeAttribute("disabled");
		this.setStatus(args[0] + " hits");
	}, this, true);
	this.onerror.subscribe(function(type, args, self) {
		this.setStatus("Error:" + args[0]);
	}, this, true);
};
maxwell.SearchDropDown.prototype.setStatus = function(txt) {
	this.searchresults.style.display = "block";
	this.status.style.display = "block";
	this.status.firstChild.innerHTML = txt;
};
maxwell.SearchDropDown.prototype.close = function() {
	this.searchresults.style.display = "none";
};
maxwell.SearchDropDown.prototype.open = function() {
	if (this.results.childNodes.length > 0) {
		this.searchresults.style.display = "block";
	}
};
maxwell.SearchDropDown.prototype.clear = function(e) {
	if (e) {
		var key = YAHOO.util.Event.getCharCode(e);
		if (key == 13) {
			this.inputBox.blur();
			this.doSearch();
			return;
		}
	}
	if (this.results.childNodes.length > 0) {
		this.searchresults.style.display = "none";
		this.results.style.display = "none";
		this.results.innerHTML = "";
	}
};
maxwell.SearchDropDown.prototype.doSearch = function() {
	if (this.doingSearch) {
		return false;
	}
	this.doingSearch = true;
	this.onstart.fire();
	this.clear();
	var self = this;
	ajaxCall("feed/find", {
		"_out" : "json",
		"query" : this.inputBox.value
	}, function(data) {
		self.doingSearch = false;
		var _5ce = data.result.length;
		self.onfinish.fire(_5ce);
		if (_5ce == 0) {
			return;
		}
		self.results.style.display = "block";
		for ( var i = 0; i < data.result.length; i++) {
			var hit = data.result[i];
			self.formatResult(hit);
		}
	}, function(_5d1) {
		self.doingSearch = false;
		self.onfinish.fire(0);
		self.onerror.fire(_5d1);
	});
	return true;
};
maxwell.SearchDropDown.prototype.formatResult = function(_5d2) {
	var node = cn("li");
	var m = maxwell.ModuleLibrary.createLibraryModule(_5d2.name,
			_5d2.description, _5d2.type);
	node.appendChild(m);
	this.results.appendChild(node);
	var rid = $id(m);
	YAHOO.util.Event.onAvailable(rid, function() {
		var _5d6 = new maxwell.DragDrop.Source(rid, _5d2, [ "rssmodule" ]);
		var _5d7 = m.getElementsByTagName("div")[1];
		YAHOO.util.Event.addListener(_5d7, "click", function() {
			var _5d8 = maxwell.PipesEditor.CurrentEditor();
			var xy = [ 250, 250 ];
			_5d8.layoutTarget.onSourceDragDrop.fire(_5d6, xy);
		}, _5d6, true);
	});
};
$namespace("maxwell.Accordion");
maxwell.Accordion = function(_5da, _5db, _5dc) {
	if (arguments.length == 0) {
		return;
	}
	this.enclosure = _5da;
	this.title_list = _5db;
	this.horizontal = _5dc;
	this.allow_all_closed = true;
	this.allow_multiple_open = false;
	if (this.horizontal) {
		this.title_class = maxwell.Accordion.horiz_title_class;
		this.content_clip_class = maxwell.Accordion.horiz_content_clip_class;
		this.content_class = maxwell.Accordion.horiz_content_class;
		this.slide_style_name = "width";
		this.slide_size_name = "offsetWidth";
	} else {
		this.title_class = maxwell.Accordion.vert_title_class;
		this.content_clip_class = maxwell.Accordion.vert_content_clip_class;
		this.content_class = maxwell.Accordion.vert_content_class;
		this.slide_style_name = "height";
		this.slide_size_name = "offsetHeight";
	}
	this.slide_time = 0.3;
	this.slide_easing = YAHOO.util.Easing.easeOut;
	this.onInit = new YAHOO.util.CustomEvent("init");
	this.onBeforeOpen = new YAHOO.util.CustomEvent("beforeOpen");
	this.onAfterOpen = new YAHOO.util.CustomEvent("afterOpen");
	this.onBeforeClose = new YAHOO.util.CustomEvent("beforeClose");
	this.onAfterClose = new YAHOO.util.CustomEvent("afterClose");
	var self = this;
	window.setTimeout(function() {
		self.init();
	}, 100);
};
maxwell.Accordion.LAYOUT_VERT = false;
maxwell.Accordion.LAYOUT_HORIZ = true;
maxwell.Accordion.debug = true;
maxwell.Accordion.agent = navigator.userAgent.toLowerCase();
maxwell.Accordion.is_ie = (maxwell.Accordion.agent.indexOf("msie") != -1 && maxwell.Accordion.agent
		.indexOf("opera") == -1);
maxwell.Accordion.min_size = (maxwell.Accordion.is_ie ? 1 : 0);
maxwell.Accordion.container_class = "yahoo-accordion-container";
maxwell.Accordion.horiz_title_class = "yahoo-accordion-title-horiz";
maxwell.Accordion.horiz_content_clip_class = "yahoo-accordion-content-clip-horiz";
maxwell.Accordion.horiz_content_class = "yahoo-accordion-content-horiz";
maxwell.Accordion.vert_title_class = "yahoo-accordion-title-vert";
maxwell.Accordion.vert_content_clip_class = "yahoo-accordion-content-clip-vert";
maxwell.Accordion.vert_content_class = "yahoo-accordion-content-vert";
maxwell.Accordion.prototype.init = function() {
	this.enclosure = YAHOO.util.Dom.get(this.enclosure);
	this.container = document.createElement("div");
	YAHOO.util.Dom.addClass(this.container, maxwell.Accordion.container_class);
	this.enclosure.appendChild(this.container);
	this.section_list = [];
	var _5de = this.title_list.length;
	for ( var i = 0; i < _5de; i++) {
		this.appendSection(this.title_list[i]);
	}
	this.title_list = null;
	this.onInit.fire();
};
maxwell.Accordion.prototype.setAnimation = function(_5e0, _5e1) {
	this.slide_time = _5e0;
	this.slide_easing = _5e1;
};
maxwell.Accordion.prototype.willAllowMultipleOpenSections = function() {
	return this.allow_multiple_open;
};
maxwell.Accordion.prototype.shouldAllowMultipleOpenSections = function(_5e2) {
	this.allow_multiple_open = _5e2;
	if (!_5e2) {
		this.closeAllSections();
	}
};
maxwell.Accordion.prototype.willAllowAllSectionsClosed = function() {
	return this.allow_all_closed;
};
maxwell.Accordion.prototype.shouldAllowAllSectionsClosed = function(_5e3) {
	this.allow_all_closed = _5e3;
	if (!_5e3 && this.allSectionsClosed()) {
		this.toggleContent(0);
	}
};
maxwell.Accordion.prototype.getTitle = function(_5e4) {
	return this.section_list[_5e4].title;
};
maxwell.Accordion.prototype.setTitle = function(_5e5, _5e6) {
	var t = this.section_list[_5e5].title;
	if (typeof _5e6 == "string") {
		t.innerHTML = _5e6;
	} else {
		this._cleanContainer(t);
		t.appendChild(_5e6);
	}
};
maxwell.Accordion.prototype.getSectionCount = function() {
	return this.section_list.length;
};
maxwell.Accordion.prototype.getSection = function(_5e8) {
	return this.section_list[_5e8];
};
maxwell.Accordion.prototype.setSection = function(_5e9, _5ea) {
	var d = this.section_list[_5e9].content;
	if (typeof _5ea == "string") {
		d.innerHTML = _5ea;
	} else {
		this._cleanContainer(d);
		d.appendChild(_5ea);
	}
};
maxwell.Accordion.prototype.prependSection = function(_5ec, _5ed) {
	return this.insertSection(0, _5ec, _5ed);
};
maxwell.Accordion.prototype.appendSection = function(_5ee, _5ef) {
	return this.insertSection(this.section_list.length, _5ee, _5ef);
};
maxwell.Accordion.prototype.insertSection = function(_5f0, _5f1, _5f2) {
	var t = document.createElement("div");
	YAHOO.util.Dom.addClass(t, this.title_class);
	var c = document.createElement("div");
	YAHOO.util.Dom.addClass(c, this.content_clip_class);
	YAHOO.util.Dom.setStyle(c, this.slide_style_name,
			maxwell.Accordion.min_size + "px");
	YAHOO.util.Dom.setStyle(c, "opacity", "0");
	var d = document.createElement("div");
	YAHOO.util.Dom.addClass(d, this.content_class);
	YAHOO.util.Dom.setStyle(d, "display", "none");
	c.appendChild(d);
	this.section_list.splice(_5f0, 0, {
		title : t,
		clip : c,
		content : d,
		open : false
	});
	if (_5f1) {
		this.setTitle(_5f0, _5f1);
	}
	if (_5f0 < this.section_list.length - 1) {
		this.container.insertBefore(t, this.section_list[_5f0 + 1].title);
	} else {
		this.container.appendChild(t);
	}
	var size = t[this.slide_size_name];
	YAHOO.util.Dom.setStyle(t, this.slide_style_name,
			maxwell.Accordion.min_size + "px");
	var _5f7 = {
		opacity : {
			from : 0,
			to : 1
		}
	};
	_5f7[this.slide_style_name] = {
		to : size
	};
	var anim = new YAHOO.util.Anim(t, _5f7, this.slide_time, this.slide_easing);
	anim.animate();
	YAHOO.util.Event.addListener(t, "click", this.onTitleClicked, this, true);
	if (_5f2) {
		this.setSection(_5f0, _5f2);
	}
	if (_5f0 < this.section_list.length - 1) {
		this.container.insertBefore(c, this.section_list[_5f0 + 1].title);
	} else {
		this.container.appendChild(c);
	}
	var _5f9 = {};
	_5f9["title"] = t;
	_5f9["content"] = d;
	return _5f9;
};
maxwell.Accordion.prototype.deleteSection = function(_5fa) {
	var _5fb = {
		opacity : {
			from : 1,
			to : 0
		}
	};
	_5fb[this.slide_style_name] = {
		to : maxwell.Accordion.min_size
	};
	if (this.section_list[_5fa].open) {
		var anim = new YAHOO.util.Anim(this.section_list[_5fa].clip, _5fb,
				this.slide_time, this.slide_easing);
		anim.animate();
	}
	var anim = new YAHOO.util.Anim(this.section_list[_5fa].title, _5fb,
			this.slide_time, this.slide_easing);
	anim.onComplete.subscribe(function(type, _5fe, _5ff) {
		_5ff[0].removeChild(_5ff[1]);
		_5ff[0].removeChild(_5ff[2]);
	}, [ this.container, this.section_list[_5fa].title,
			this.section_list[_5fa].clip ]);
	anim.animate();
	this.section_list.splice(_5fa, 1);
	if (!this.allow_all_closed && this.allSectionsClosed()) {
		this.toggleContent(0);
	}
};
maxwell.Accordion.prototype.findSection = function(_600) {
	var _601 = this.section_list.length;
	for ( var i = 0; i < _601; i++) {
		if (this.section_list[i].content == _600) {
			return i;
		}
	}
	return null;
};
maxwell.Accordion.prototype.findSectionByTitle = function(_603) {
	var _604 = this.section_list.length;
	for ( var i = 0; i < _604; i++) {
		if (this.section_list[i].title.innerHTML == _603) {
			return i;
		}
	}
	return null;
};
maxwell.Accordion.prototype.sectionIsOpen = function(_606) {
	return this.section_list[_606].open;
};
maxwell.Accordion.prototype.openSection = function(_607) {
	if (!this.section_list[_607].open) {
		this.toggleContent(_607);
	}
};
maxwell.Accordion.prototype.closeSection = function(_608) {
	if (this.section_list[_608].open) {
		this.toggleContent(_608);
	}
};
maxwell.Accordion.prototype.allSectionsClosed = function() {
	var _609 = this.section_list.length;
	for ( var i = 0; i < _609; i++) {
		if (this.section_list[i].open) {
			return false;
		}
	}
	return true;
};
maxwell.Accordion.prototype.setHandler = function(_60b, _60c) {
	this.section_list[_60b].handler = _60c;
	if (_60c && _60c.init && typeof _60c.init == "function") {
		_60c.init(this, _60b);
	}
};
maxwell.Accordion.prototype.onTitleClicked = function(e) {
	var t = YAHOO.util.Event.getTarget(e);
	var _60f = this.section_list.length;
	for ( var i = 0; i < _60f; i++) {
		if (t == this.section_list[i].title) {
			this.toggleContent(i);
			break;
		}
	}
};
maxwell.Accordion.prototype.toggleContent = function(_611) {
	if (!this.section_list[_611].open && !this.allow_multiple_open) {
		var save = this.allow_all_closed;
		this.allow_all_closed = true;
		this.closeAllSections();
		this.allow_all_closed = save;
	} else {
		if (this.section_list[_611].open && !this.allow_all_closed) {
			this.section_list[_611].open = false;
			if (this.allSectionsClosed()) {
				this.section_list[_611].open = true;
				return;
			}
			this.section_list[_611].open = true;
		}
	}
	if (!this.section_list[_611].open) {
		YAHOO.util.Dom.setStyle(this.section_list[_611].content, "display",
				"block");
		this.onBeforeOpen.fire(_611);
		var h = this.section_list[_611].handler;
		if (h && h.onShow && typeof h.onShow == "function") {
			h.onShow();
		}
		var size = this.section_list[_611].content[this.slide_size_name];
		var _615 = {
			opacity : {
				from : 0,
				to : 1
			}
		};
		_615[this.slide_style_name] = {
			to : size
		};
		var anim = new YAHOO.util.Anim(this.section_list[_611].clip, _615,
				this.slide_time, this.slide_easing);
		anim.onComplete.subscribe(function(type, _618, _619) {
			_619[0].onAfterOpen.fire(_619[1]);
		}, [ this, _611 ]);
		anim.animate();
		this.section_list[_611].open = true;
	} else {
		this.onBeforeClose.fire(_611);
		var _615 = {
			opacity : {
				from : 1,
				to : 0
			}
		};
		_615[this.slide_style_name] = {
			to : maxwell.Accordion.min_size
		};
		var anim = new YAHOO.util.Anim(this.section_list[_611].clip, _615,
				this.slide_time, this.slide_easing);
		anim.onComplete.subscribe(function(type, _61b, _61c) {
			YAHOO.util.Dom.setStyle(_61c[2], "display", "none");
			_61c[0].onAfterClose.fire(_61c[1]);
			if (_61c[3] && _61c[3].onShow
					&& typeof _61c[3].onShow == "function") {
				_61c[3].onHide();
			}
		}, [ this, _611, this.section_list[_611].content,
				this.section_list[_611].handler ]);
		anim.animate();
		this.section_list[_611].open = false;
	}
};
maxwell.Accordion.prototype.closeAllSections = function() {
	var _61d = this.section_list.length;
	var _61e = true;
	for ( var i = 0; i < _61d; i++) {
		if (this.section_list[i].open) {
			if (!this.allow_all_closed && _61e) {
				_61e = false;
			} else {
				this.toggleContent(i);
			}
		}
	}
	if (!this.allow_all_closed && _61e) {
		this.toggleContent(0);
	}
};
maxwell.Accordion.prototype._cleanContainer = function(obj) {
	while (obj.hasChildNodes()) {
		obj.removeChild(obj.lastChild);
	}
};
$namespace("maxwell.TerminalMenu");
maxwell.TerminalMenu = function(t) {
	this.terminal = t;
	this.config = t.getRenderStyle();
	this.id = maxwell.Util.newId("terminaltool");
	this.node = cn("div", {
		className : "wiretool"
	}, {}, "<span></span>");
	this.updateType(t.config.types);
	this.img = cn("div", {
		className : "wiretoolcut"
	}, {
		display : "none"
	});
	this.node.appendChild(this.img);
	this.terminal.ontypechange.subscribe(function(type, args, self) {
		this.updateType([ args[0] ]);
	}, this, true);
	YAHOO.util.Event
			.addListener(this.img, "click", this.removeLine, this, true);
	this.node.style.display = "none";
	t.node.appendChild(this.node);
	if (t.config.dynamicType) {
		this.terminal.ontypechange.subscribe(function(type, args, self) {
			this.updateType([ args[0] ]);
		}, this, true);
	}
	t.onnewwire.subscribe(function(type, args, me) {
		this.processNewWire(args[0]);
	}, this, true);
	t.onremovewire.subscribe(function(type, args, me) {
		this.processRemoveWire(args[0]);
	}, this, true);
	t.onremove.subscribe(this.remove, this, true);
	YAHOO.util.Event
			.addListener(t.node, "mouseover", this.showMenu, this, true);
	YAHOO.util.Event.addListener(this.node, "mouseover", this.showMenu, this,
			true);
	YAHOO.util.Event.addListener(t.node, "mouseout", this.hide, this, true);
	YAHOO.util.Event.addListener(this.node, "mouseout", this.hide, this, true);
};
maxwell.TerminalMenu.prototype.updateType = function(_62e) {
	if (!_62e || _62e.length == 0 || !_62e[0]) {
		this.node.firstChild.innerHTML = "unset";
		return;
	}
	var txt = "";
	for ( var i = 0; i < _62e.length; i++) {
		txt += _62e[i] + " ";
	}
	this.node.firstChild.innerHTML = txt;
};
maxwell.TerminalMenu.prototype.processNewWire = function(wire) {
	YAHOO.util.Event.addListener(wire.node, "mouseover", this.showNoMenu, this,
			true);
	YAHOO.util.Event.addListener(wire.node, "mouseout", this.hide, this, true);
	this.img.style.display = "inline";
};
maxwell.TerminalMenu.prototype.processRemoveWire = function(wire) {
	YAHOO.util.Event.removeListener(wire.node, "mouseover", this.showNoMenu);
	YAHOO.util.Event.removeListener(wire.node, "mouseout", this.hide);
	this.setOpacity(1, wire);
	this.img.style.display = "none";
};
maxwell.TerminalMenu.prototype.hide = function() {
	if (this.showTimeout) {
		window.clearTimeout(this.showTimeout);
		this.showTimeout = null;
	}
	if (this.hideTimeout) {
		return;
	}
	var self = this;
	this.hideTimeout = window.setTimeout(function() {
		self.node.style.display = "none";
		self.hideTimeout = null;
		self.setOpacity(1);
	}, 500);
};
maxwell.TerminalMenu.prototype.showMenu = function() {
	this.show(true);
};
maxwell.TerminalMenu.prototype.showNoMenu = function() {
	var ab = this.getAB();
	if (ab && (ab.a.dragging || ab.b.dragging)) {
		return;
	}
	this.show(false);
};
maxwell.TerminalMenu.prototype.show = function(_635) {
	this.showMenu = _635;
	if (this.hideTimeout) {
		window.clearTimeout(this.hideTimeout);
		this.hideTimeout = null;
	}
	if (this.showTimeout) {
		return;
	}
	var self = this;
	this.showTimeout = window.setTimeout(function() {
		if (self.showMenu) {
			self.node.style.display = "block";
			var xy = YAHOO.util.Dom.getXY(self.terminal.node);
			YAHOO.util.Dom.setXY(self.node, [
					xy[0] + self.terminal.direction[0] * 20
							- (self.node.offsetWidth / 2),
					xy[1] + self.terminal.direction[1] * 20
							- (self.node.offsetHeight / 2) ]);
		}
		self.setOpacity(0.7);
	}, 500);
};
maxwell.TerminalMenu.prototype.removeLine = function() {
	this.hide();
	if (this.terminal.wires[0]) {
		this.terminal.wires[0].remove();
	}
};
maxwell.TerminalMenu.prototype.remove = function() {
	if (!this.removed) {
		this.removed = true;
		this.node.parentNode.removeChild(this.node);
	}
};
maxwell.TerminalMenu.prototype.getAB = function(wire) {
	if (!wire) {
		if (this.terminal.wires.length == 0) {
			return;
		}
		wire = this.terminal.wires[0];
	}
	if (wire.removed) {
		return null;
	}
	var a = wire.src.block;
	if (a.node.getAttribute("submodule") === "true") {
		a = maxwell.Module.getModule(a.node);
	}
	var b = wire.tgt.block;
	if (b.node.getAttribute("submodule") === "true") {
		b = maxwell.Module.getModule(b.node.parentNode);
	}
	return {
		a : a,
		b : b
	};
};
maxwell.TerminalMenu.prototype.setOpacity = function(v, wire) {
	var ab = this.getAB(wire);
	if (!ab) {
		return;
	}
	var a = ab.a.node.firstChild;
	var b = ab.b.node.firstChild;
	var f = function(n, v) {
		if (!n) {
			return;
		}
		if (v == 1) {
			var b = new YAHOO.util.Anim(n, {
				opacity : {
					to : 1
				}
			}, 0.3, YAHOO.util.Easing.easeOut);
			b.onComplete.subscribe(function(type, args, me) {
				if (maxwell.Util.ua.isIE) {
					n.style.filter = "none;";
				}
			}, this, true);
			b.animate();
		} else {
			var b = new YAHOO.util.Anim(n, {
				opacity : {
					to : v
				}
			}, 0.3, YAHOO.util.Easing.easeIn);
			b.animate();
		}
	};
	f(a, v);
	f(b, v);
};
$namespace("maxwell.SimpleTree");
maxwell.SimpleTree = function(node) {
	if (!node) {
		this.node = cn("div", {
			className : "simpletree"
		});
		this.root = cn("ul", {
			className : "root"
		});
		this.node.appendChild(this.root);
	} else {
		this.node = node;
		if (!YAHOO.util.Dom.hasClass(this.node, "simpletree")) {
			YAHOO.util.Dom.addClass(this.node, "simpletree");
		}
		this.root = node.getElementsByTagName("ul")[0];
		if (!YAHOO.util.Dom.hasClass(this.root, "root")) {
			YAHOO.util.Dom.addClass(this.root, "root");
		}
	}
	this.onbranchopen = new YAHOO.util.CustomEvent("onbranchopen", this, true);
	YAHOO.util.Event.addListener(this.node, "click", this.click, this, true);
};
maxwell.SimpleTree.prototype.click = function(_648) {
	var el = YAHOO.util.Event.getTarget(_648, true);
	while (el && el.tagName != "LI" && el.tagName != "UL" && el != this.node) {
		el = el.parentNode;
	}
	if (!el || el == this.node || el == this.root) {
		return;
	}
	var _64a = function(el) {
		if (YAHOO.util.Dom.hasClass(el, "closed")) {
			YAHOO.util.Dom.removeClass(el, "closed");
		} else {
			YAHOO.util.Dom.addClass(el, "closed");
		}
	};
	var ul = null;
	var li = null;
	if (el.tagName == "UL") {
		ul = el;
		_64a(el);
		if (el.parentNode && el.parentNode.tagName == "LI") {
			li = el.parentNode;
			_64a(li);
		}
	} else {
		if (YAHOO.util.Dom.hasClass(el, "branch")) {
			li = el;
			_64a(el);
			ul = el.getElementsByTagName("ul")[0];
			_64a(ul);
		} else {
			return;
		}
	}
	this.onbranchopen.fire(ul, li);
	YAHOO.util.Event.stopEvent(_648);
};
maxwell.SimpleTree.prototype.getKeyChain = function(node) {
	var keys = [];
	while (node != this.root) {
		var key = node.getAttribute("key");
		if (key) {
			keys.push(key);
		}
		node = node.parentNode;
	}
	return keys;
};
maxwell.SimpleTree.prototype.clear = function() {
	this.root.innerHTML = "";
};
maxwell.SimpleTree.prototype.createNode = function(_651, key, _653) {
	var _654 = {};
	if (typeof (key) != "undefined" && key != null) {
		_654["key"] = "" + key;
	}
	if (_653) {
		_654["className"] = "branch closed";
	}
	var n = cn("li", _654, {}, "<span>" + _651 + "</span>");
	if (_653) {
		n.appendChild(cn("ul", {
			className : "closed"
		}, {}));
	}
	return n;
};
maxwell.SimpleTree.prototype.addNode = function(_656, node) {
	if (_656.tagName == "LI") {
		if (YAHOO.util.Dom.hasClass(_656, "branch")) {
			_656 = _656.getElementsByTagName("ul")[0];
		} else {
			var _658 = cn("ul", {
				className : "closed"
			}, {});
			_656.appendChild(_658);
			YAHOO.util.Dom.addClass(_656, "branch");
			YAHOO.util.Dom.addClass(_656, "closed");
			_656 = _658;
		}
	}
	_656.appendChild(node);
};
$namespace("maxwell.Dialog");
maxwell.Dialog = function(_659, _65a) {
	if (!_659) {
		_659 = {
			content : ""
		};
	} else {
		if (typeof (_659) == "string") {
			_659 = {
				content : _659
			};
		}
	}
	this.config = _659;
	this.node = cn(
			"div",
			{
				className : "dialog"
			},
			{
				zIndex : 9999,
				position : "absolute",
				visibility : "hidden"
			},
			"<div class='dialogheader'></div><div class='dialogbody'></div><div class='dialogfooter'>");
	if (_659.className) {
		YAHOO.util.Dom.addClass(this.node, _659.className);
	}
	var divs = this.node.getElementsByTagName("div");
	this.header = divs[0];
	this.body = divs[1];
	this.footer = divs[2];
	if (!_659.body) {
		this.body.style.display = "none";
	} else {
		if (typeof (_659.body) == "string") {
			this.body.innerHTML = _659.body;
		} else {
			this.body.appendChild(_659.body);
		}
		this.body.style.display = "block";
	}
	if (!_659.content) {
		_659.content = "";
	}
	if (typeof (_659.content) == "string") {
		this.header.innerHTML = _659.content;
	} else {
		this.header.appendChild(_659.content);
	}
	var _65c = {};
	if (_65a) {
		_65c = YAHOO.util.Dom.getRegion(_65a);
	} else {
		_65c = {
			left : 0,
			top : 0,
			bottom : YAHOO.util.Dom.getViewportHeight(),
			right : YAHOO.util.Dom.getViewportWidth()
		};
		_65a = document.body;
	}
	YAHOO.util.Dom.setStyle(this.node, "opacity", 0);
	_65a.appendChild(this.node);
	if (!_659.noBackground) {
		if (!_659.bgcolor) {
			_659.bgcolor = "#202020";
		}
		var _65d = {
			className : "dialogbg",
			width : (_65c.right - _65c.left) + "px",
			height : (_65c.bottom - _65c.top) + "px"
		};
		var _65e = {
			textAlign : "center",
			verticalAlign : "middle",
			backgroundColor : _659.bgcolor,
			border : "0px",
			margin : "0px",
			padding : "0px",
			display : "block",
			position : "absolute",
			zIndex : 9998,
			left : _65c.left + "px",
			top : _65c.top + "px",
			width : _65d.width,
			height : _65d.height
		};
		this.bgnode = cn("div", _65d, _65e);
		if (maxwell.Util.ua.isIE) {
			_65e.filter = "alpha(opacity=0)";
			_65e.zIndex = 1;
			var _65f = cn("iframe", _65d, _65e);
			this.bgnode.appendChild(_65f);
		}
		YAHOO.util.Dom.setStyle(this.bgnode, "opacity", 0.3);
		_65a.appendChild(this.bgnode);
	}
	if (!_659.position) {
		YAHOO.util.Dom.setXY(this.node, [
				(_65c.right - _65c.left - this.node.offsetWidth) / 2,
				(_65c.bottom - _65c.top - this.node.offsetHeight) / 2 ]);
	} else {
		sn(this.node, null, {
			top : "16px",
			right : "16px"
		});
	}
	this.node.style.visibility = "visible";
	this.display();
};
maxwell.Dialog.prototype.display = function() {
	if (maxwell.Util.ua.isIE) {
		this.node.style.filter = "none";
		return;
	}
	this.anim2 = new YAHOO.util.Anim(this.node, {
		opacity : {
			to : 1
		}
	}, 0.3, YAHOO.util.Easing.easeOut);
	if (this.bgnode) {
		this.anim1 = new YAHOO.util.Anim(this.bgnode, {
			opacity : {
				to : 0.3
			}
		}, 0.3, YAHOO.util.Easing.easeOut);
		this.anim1.animate();
	}
	this.anim2.animate();
};
maxwell.Dialog.prototype.remove = function() {
	if (maxwell.Util.ua.isIE) {
		if (this.bgnode) {
			this.bgnode.parentNode.removeChild(this.bgnode);
		}
		this.node.parentNode.removeChild(this.node);
		return;
	}
	this.anim2 = new YAHOO.util.Anim(this.node, {
		opacity : {
			to : 0
		}
	}, 0.3, YAHOO.util.Easing.easeIn);
	if (this.bgnode) {
		this.anim1 = new YAHOO.util.Anim(this.bgnode, {
			opacity : {
				to : 0
			}
		}, 0.3, YAHOO.util.Easing.easeIn);
		this.anim1.onComplete.subscribe(function(type, args, me) {
			if (this.bgnode.parentNode) {
				this.bgnode.parentNode.removeChild(this.bgnode);
			}
		}, this, true);
	}
	this.anim2.onComplete.subscribe(function(type, args, me) {
		this.node.parentNode.removeChild(this.node);
	}, this, true);
	if (this.bgnode) {
		this.anim1.animate();
	}
	this.anim2.animate();
};
maxwell.Dialog.Alert = function(_666, _667) {
	maxwell.Dialog.Alert.superclass.constructor.call(this, _666, _667);
	this.header.style.fontWeight = "bold";
	if (!_666.oktext) {
		_666.oktext = "OK";
	}
	var _668 = cn("div", {
		className : "dialogbuttons"
	}, null, "<ul></ul>");
	this.okButton = maxwell.Toolbar.createBigButton(_666.oktext);
	_668.firstChild.appendChild(this.okButton);
	this.body.appendChild(_668);
	this.body.style.display = "block";
	this.okButton.focus();
	YAHOO.util.Event.addListener(this.okButton, "click", function() {
		this.remove();
		if (_666.onOk) {
			_666.onOk();
		}
	}, this, true);
};
$extend(maxwell.Dialog.Alert, maxwell.Dialog);
maxwell.Dialog.OkCancel = function(_669, _66a) {
	maxwell.Dialog.OkCancel.superclass.constructor.call(this, _669, _66a);
	if (!_669.oktext) {
		_669.oktext = "OK";
	}
	if (!_669.canceltext) {
		_669.canceltext = "Cancel";
	}
	var _66b = cn("div", {
		className : "dialogbuttons"
	}, null, "<ul></ul>");
	this.okButton = maxwell.Toolbar.createBigButton(_669.oktext);
	this.cancelButton = maxwell.Toolbar.createBigButton(_669.canceltext);
	this.buttons = _66b;
	_66b.firstChild.appendChild(this.okButton);
	_66b.firstChild.appendChild(this.cancelButton);
	this.body.appendChild(_66b);
	this.body.style.display = "block";
	YAHOO.util.Event.addListener(this.okButton, "click", function() {
		this.remove();
		if (_669.onOk) {
			_669.onOk();
		}
	}, this, true);
	YAHOO.util.Event.addListener(this.cancelButton, "click", function() {
		this.remove();
		if (_669.onCancel) {
			_669.onCancel();
		}
	}, this, true);
};
$extend(maxwell.Dialog.OkCancel, maxwell.Dialog);
maxwell.Dialog.Draggable = function(_66c, _66d) {
	if (!_66c) {
		_66c = {};
	}
	if (!_66c.className) {
		_66c.className = "dialogdraggable";
	} else {
		_66c.className += " dialogdraggable";
	}
	_66c.noBackground = true;
	maxwell.Dialog.Draggable.superclass.constructor.call(this, _66c, _66d);
	this.closeButton = cn("div", {
		className : "closebutton"
	});
	this.header.appendChild(this.closeButton);
	YAHOO.util.Event.addListener(this.closeButton, "click", function() {
		this.remove();
		if (_66c.onOk) {
			_66c.onOk();
		}
	}, this, true);
	this.dd = new YAHOO.util.DDProxy($id(this.node));
	if (maxwell.Util.ua.isIE) {
		this.dd.onMouseDown = function(x, y) {
			document.body.ondrag = function() {
				return false;
			};
			document.body.onselectstart = function() {
				return false;
			};
		};
		this.dd.onMouseUp = function(_670) {
			document.body.ondrag = null;
			document.body.onselectstart = null;
		};
	}
};
$extend(maxwell.Dialog.Draggable, maxwell.Dialog.Alert);
maxwell.Dialog.Draggable.prototype.remove = function() {
	this.dd.unreg();
	maxwell.Dialog.Draggable.superclass.remove.call(this);
};
maxwell.Dialog.Iframe = function(_671, _672) {
	if (!_671) {
		_671 = {};
	}
	if (!_671.className) {
		_671.className = "dialogiframe";
	}
	if (!_671.url) {
		_671.url = "about:blank";
	} else {
		if (maxwell.Util.ua.isSafari && _671.url[0] == "/") {
			_671.url = window.location.protocol + "//" + window.location.host
					+ _671.url;
		}
	}
	_671.body = "<iframe src=\"" + _671.url
			+ "\" frameborder=0 class=\"dialogiframebody\" />";
	maxwell.Dialog.Iframe.superclass.constructor.call(this, _671, _672);
};
$extend(maxwell.Dialog.Iframe, maxwell.Dialog.Draggable);
maxwell.Dialog.Save = function(_673, _674) {
	_673.content = "<h1>Pipe name:</h1><input type=text class='dialogsaveinput' /><div>";
	_673.className = "savedialog";
	maxwell.Dialog.Save.superclass.constructor.call(this, _673, _674);
	var _675 = cn("div", {
		className : "dialogbuttons"
	}, null, "<ul></ul>");
	this.okButton = maxwell.Toolbar.createBigButton("Save");
	this.cancelButton = maxwell.Toolbar.createBigButton("Cancel");
	_675.firstChild.appendChild(this.okButton);
	_675.firstChild.appendChild(this.cancelButton);
	this.header.appendChild(_675);
	var inp = this.header.getElementsByTagName("input")[0];
	var self = this;
	maxwell.HtmlPlus.Util.manageEnter(inp, function() {
		self.onOk();
	});
	inp.focus();
	YAHOO.util.Event.addListener(this.okButton, "click", this.onOk, this, true);
	YAHOO.util.Event.addListener(this.cancelButton, "click", this.onCancel,
			this, true);
};
$extend(maxwell.Dialog.Save, maxwell.Dialog);
maxwell.Dialog.Save.prototype.onOk = function() {
	if (this.config.onOk) {
		var val = this.header.getElementsByTagName("input")[0].value;
		this.config.onOk(val);
	}
	this.remove();
};
maxwell.Dialog.Save.prototype.onCancel = function() {
	this.remove();
	if (this.config.onCancel) {
		this.config.onCancel();
	}
};
$namespace("maxwell.SimpleTooltip");
maxwell.SimpleTooltip = function(_679, tip, _67b) {
	if (!tip) {
		return;
	}
	this.target = _679;
	if (_67b) {
		for ( var n in _67b) {
			this.config[n] = _67b[n];
		}
	}
	this.tip = tip;
	this.shown = false;
	var id = $id(this.target);
	YAHOO.util.Event.onAvailable(id, this.initevents, this);
};
maxwell.SimpleTooltip.prototype.initevents = function(self) {
	YAHOO.util.Event.addListener(self.target, "mouseover", function(ev) {
		var xy = YAHOO.util.Event.getXY(ev);
		this.show(xy);
	}, self, true);
	YAHOO.util.Event.addListener(self.target, "mouseout", function(ev) {
		this.hide();
	}, self, true);
};
maxwell.SimpleTooltip.prototype.show = function(xy) {
	if (this.hideTimeout) {
		window.clearTimeout(this.hideTimeout);
		this.hideTimeout = null;
	}
	if (this.showTimeout) {
		return;
	}
	var self = this;
	var pos = xy;
	this.showTimeout = window.setTimeout(function() {
		self.doShow(pos);
	}, this.config.hoverTime);
};
maxwell.SimpleTooltip.prototype.doShow = function(xy) {
	if (this.shown) {
		return false;
	}
	if (this.hideTimeout) {
		window.clearTimeout(this.hideTimeout);
		this.hideTimeout = null;
	}
	if (this.showTimeout) {
		window.clearTimeout(this.showTimeout);
		this.showTimeout = null;
	}
	var _686 = YAHOO.util.Dom.getRegion(this.target);
	if (!xy) {
		xy = [ (_686.left + _686.right) / 2, (_686.top + _686.bottom) / 2 ];
	} else {
		xy[1] = (_686.top + _686.bottom) / 2;
	}
	if (!this.node) {
		this.node = this.createNode(xy);
		this.shim = maxwell.Util.addShim(this.node);
		document.body.appendChild(this.node);
	} else {
		this.positionNode(xy);
	}
	this.shown = true;
	return true;
};
maxwell.SimpleTooltip.prototype.createNode = function(xy) {
	return cn("div", {
		className : "simpletooltip"
	}, {
		position : "absolute",
		display : "block",
		left : xy[0] + "px",
		top : xy[1] + "px"
	}, this.tip);
};
maxwell.SimpleTooltip.prototype.positionNode = function(xy) {
	sn(this.node, null, {
		left : xy[0] + "px",
		top : xy[1] + "px",
		display : "block"
	});
};
maxwell.SimpleTooltip.prototype.hide = function(ev) {
	if (this.showTimeout) {
		window.clearTimeout(this.showTimeout);
		this.showTimeout = null;
	}
	if (this.hideTimeout) {
		return;
	}
	var self = this;
	this.hideTimeout = window.setTimeout(function() {
		self.doHide();
	}, this.config.hoverTime);
};
maxwell.SimpleTooltip.prototype.doHide = function() {
	if (this.node) {
		this.node.style.display = "none";
	}
	if (this.hideTimeout) {
		window.clearTimeout(this.hideTimeout);
		this.hideTimeout = null;
	}
	if (this.showTimeout) {
		window.clearTimeout(this.showTimeout);
		this.showTimeout = null;
	}
	this.shown = false;
};
maxwell.SimpleTooltip.prototype.config = {
	hoverTime : 1000
};
$namespace("maxwell.LargeTooltip");
maxwell.LargeTooltip = function(_68b, _68c, _68d) {
	maxwell.LargeTooltip.superclass.constructor.call(this, _68b, _68c, _68d);
	this.xdelta = 20;
	this.ydelta = -50;
	this.maxw = 168;
	this.minw = 168;
	this.maxh = 350;
	this.minh = 60;
	this.dx = 16;
	this.dy = 16;
};
$extend(maxwell.LargeTooltip, maxwell.SimpleTooltip);
maxwell.LargeTooltip.prototype.createNode = function(xy) {
	var l = xy[0] + this.xdelta;
	var t = xy[1] + this.ydelta;
	this.tip = "/pipes/docs/module.php?type=" + this.tip;
	var node = cn(
			"div",
			{
				className : "tooltipholder"
			},
			{
				marginLeft : "-10000px",
				zIndex : 9998,
				position : "absolute",
				left : l + "px",
				top : t + "px"
			},
			"<div class='tooltipheader'></div><div class='tooltipbody'><iframe frameborder=0 class='tooltipframe' src='about:blank'></iframe></div><div class='tooltipfooter'></div>");
	var _692 = cn("div", {
		className : "tooltiparrow"
	}, {
		zIndex : 9999,
		position : "absolute",
		left : -this.xdelta + "px",
		top : -(this.ydelta + 8) + "px"
	});
	node.appendChild(_692);
	this.iframe = node.getElementsByTagName("iframe")[0];
	this.iframe.setAttribute("src", this.tip);
	YAHOO.util.Event.onAvailable($id(this.iframe), this.getFrameInfo, this,
			true);
	return node;
};
maxwell.LargeTooltip.prototype.getFrameInfo = function(self) {
	if (maxwell.Util.ua.isIE) {
		self.idoc = self.iframe.contentWindow.document;
		self.iwin = self.iframe.contentWindow;
	} else {
		self.idoc = self.iframe.contentDocument;
		self.iwin = self.iframe.contentWindow;
	}
};
maxwell.LargeTooltip.prototype.positionNode = function(xy) {
	var l = xy[0] + this.xdelta;
	var t = xy[1] + this.ydelta;
	sn(this.node, null, {
		marginLeft : "-10000px",
		left : l + "px",
		top : t + "px",
		display : "block"
	});
	sn(this.node.lastChild, null, {
		left : -this.xdelta + "px",
		top : -(this.ydelta + 8) + "px"
	});
};
maxwell.LargeTooltip.prototype.doShow = function(xy) {
	var _698 = maxwell.LargeTooltip.superclass.doShow.call(this, xy);
	if (_698) {
		YAHOO.util.Event.addListener(document.body, "mouseover", this.docFocus,
				this, true);
		YAHOO.util.Event.addListener(this.node, "mouseover", this.tipFocus,
				this, true);
		this.getHeight();
	}
	if (this.iwin) {
		this.iwin.focus();
	}
};
maxwell.LargeTooltip.prototype.doHide = function() {
	if (this.shown) {
		var a = YAHOO.util.Event.removeListener(document.body, "mouseover",
				this.docFocus);
		var b = YAHOO.util.Event.removeListener(this.node, "mouseover",
				this.tipFocus);
		this.iframe.style.height = "1px";
		this.node.style.marginLeft = "-10000px";
	}
	maxwell.LargeTooltip.superclass.doHide.call(this);
};
maxwell.LargeTooltip.prototype.docFocus = function(ev) {
	this.hide();
};
maxwell.LargeTooltip.prototype.tipFocus = function(ev) {
	YAHOO.util.Event.stopEvent(ev);
	this.show();
};
maxwell.LargeTooltip.prototype.getHeight = function() {
	if (!this.count) {
		this.count = 0;
	}
	if (!this.shown) {
		return;
	}
	var wh = [ 0, 0 ];
	if (this.iwin && this.idoc) {
		wh = this.calcWH(this.iwin, this.idoc);
	} else {
		this.getFrameInfo(this);
	}
	if (wh[1] == 0 || wh[1] == this.iframe.offsetHeight) {
		this.count++;
		if (this.count < 20) {
			var self = this;
			window.setTimeout(function() {
				self.getHeight();
			}, 250);
			return;
		}
		this.count = 0;
		return;
	}
	wh[1] += 8;
	if (wh[1] > this.maxh) {
		wh[1] = this.maxh;
	}
	if (wh[1] < this.minh) {
		wh[1] = this.minh;
	}
	this.iframe.style.height = wh[1] + "px";
	YAHOO.util.Dom.setStyle(this.node, "opacity", 0);
	this.node.style.marginLeft = "0px";
	if (this.node.offsetTop < 0) {
		var _69f = this.node.offsetTop;
		this.node.style.top = "0px";
		this.node.lastChild.style.top = this.node.lastChild.offsetTop + _69f
				+ "px";
	}
	if (this.node.offsetTop + this.node.offsetHeight > document.body.offsetHeight) {
		var _6a0 = document.body.offsetHeight - this.node.offsetHeight;
		var _69f = this.node.offsetTop - _6a0;
		this.node.style.top = _6a0 + "px";
		this.node.lastChild.style.top = this.node.lastChild.offsetTop + _69f
				+ "px";
	}
	this.shim.rsize(this.shim);
	var a = new YAHOO.util.Anim(this.node, {
		opacity : {
			to : 1
		}
	}, 0.25, YAHOO.util.Easing.easeOut);
	a.animate();
};
maxwell.LargeTooltip.prototype.calcWH = function(win, doc) {
	var _6a4 = 0, sh, oh;
	if (win.innerHeight && win.scrollMaxY) {
		_6a4 = win.innerHeight + win.scrollMaxY;
	} else {
		if (doc.height) {
			_6a4 = doc.height;
		} else {
			if (doc.body) {
				if (doc.body.scrollHeight) {
					_6a4 = sh = doc.body.scrollHeight;
				}
				if (doc.body.offsetHeight) {
					_6a4 = oh = doc.body.offsetHeight;
				}
				if (sh && oh) {
					_6a4 = Math.max(sh, oh);
				}
			}
		}
	}
	var _6a7 = 0, sw, ow;
	if (win.innerWidth && win.scrollMaxX) {
		_6a7 = win.innerWidth + win.scrollMaxX;
	} else {
		if (doc.width) {
			_6a7 = doc.width;
		} else {
			if (doc.body) {
				if (doc.body.scrollWidth) {
					_6a7 = sw = doc.body.scrollWidth;
				}
				if (doc.body.offsetWidth) {
					_6a7 = ow = doc.body.offsetWidth;
				}
				if (sw && ow) {
					_6a7 = Math.max(sw, ow);
				}
			}
		}
	}
	return [ _6a7, _6a4 ];
};
$namespace("maxwell.Taglist");
maxwell.Taglist = function(tags, node) {
	if (!node) {
		this.node = cn("div", {
			className : "taglist"
		});
	} else {
		this.node = node;
		if (!YAHOO.util.Dom.hasClass(this.node, "taglist")) {
			YAHOO.util.Dom.addClass(this.node, "taglist");
		}
		this.ul = node.getElementsByTagName("ul")[0];
	}
	if (!this.ul) {
		this.ul = cn("ul");
		this.node.appendChild(this.ul);
	}
	this.inputli = cn(
			"li",
			null,
			null,
			"<input type=text><img class='add' src='http://l.yimg.com/a/i/space.gif' width=16 height=16>");
	this.input = this.inputli.firstChild;
	this.ul.appendChild(this.inputli);
	this.addTags(tags);
	var self = this;
	YAHOO.util.Event.addListener(self.input, "keydown", self.keydown, self,
			true);
	YAHOO.util.Event.addListener(self.node, "click", self.click, self, true);
	maxwell.HtmlPlus.Util.manageInput(this.input, "enter tag");
};
maxwell.Taglist.prototype.keydown = function(_6ad) {
	var _6ae = YAHOO.util.Event.getCharCode(_6ad);
	if (_6ae == 32 || _6ae == 13) {
		this.addInputTag();
		YAHOO.util.Event.stopEvent(_6ad);
	}
};
maxwell.Taglist.prototype.addInputTag = function() {
	if (this.input.value.length > 0
			&& !YAHOO.util.Dom.hasClass(this.input, "novalue")) {
		var val = this.input.value;
		this.input.value = "";
		this.addTags([ val ]);
		return true;
	}
	return false;
};
maxwell.Taglist.prototype.click = function(_6b0) {
	var el = YAHOO.util.Event.getTarget(_6b0, true);
	if (el.tagName == "IMG") {
		if (el.parentNode == this.inputli) {
			this.addInputTag();
		} else {
			this.ul.removeChild(el.parentNode);
		}
		YAHOO.util.Event.stopEvent(_6b0);
	}
};
maxwell.Taglist.prototype.addTags = function(tags) {
	for ( var i = 0; i < tags.length; i++) {
		if (!this.hasTag(tags[i])) {
			var li = cn(
					"li",
					null,
					null,
					"<span>"
							+ tags[i]
							+ "</span><img class='del' src='http://l.yimg.com/a/i/space.gif' width=16 height=16>");
			this.ul.insertBefore(li, this.inputli);
		}
	}
};
maxwell.Taglist.prototype.hasTag = function(tag) {
	for ( var i = 0; i < this.ul.childNodes.length; i++) {
		if (this.ul.childNodes[i].firstChild.innerHTML == tag) {
			return true;
		}
	}
	return false;
};
maxwell.Taglist.prototype.getTags = function() {
	var tags = [];
	for ( var i = 0; i < this.ul.childNodes.length - 1; i++) {
		tags.push(this.ul.childNodes[i].firstChild.innerHTML);
	}
	return tags;
};
maxwell.Taglist.prototype.clear = function() {
	while (this.ul.firstChild && this.ul.firstChild != this.inputli) {
		this.ul.removeChild(this.ul.firstChild);
	}
};
$namespace("maxwell.Splitter");
maxwell.Splitter = function(_6b9, _6ba, _6bb) {
	if (!_6bb) {
		this.config = {};
	} else {
		this.config = _6bb;
	}
	this.node = cn("div", {
		className : "splitter " + this.config.className
	}, {
		display : "block",
		position : "absolute",
		zoom : "1"
	});
	this.nodeA = _6b9;
	this.nodeB = _6ba;
	_6b9.parentNode.appendChild(this.node);
	this.shown = true;
	var tab = this.createTab();
	if (tab) {
		this.node.appendChild(tab);
		YAHOO.util.Event.addListener(tab.firstChild, "click", this.openclose,
				this, true);
	}
};
maxwell.Splitter.prototype.openclose = function() {
	if (this.shown) {
		this.collapse();
	} else {
		this.expand();
	}
};
maxwell.Splitter.prototype.collapse = function() {
	this.shown = false;
	if (this.animating) {
		return;
	}
	this.animating = true;
	this.animateHide();
};
maxwell.Splitter.prototype.expand = function() {
	this.shown = true;
	if (this.animating) {
		return;
	}
	this.animating = true;
	this.animateShow();
};
maxwell.Splitter.prototype.createTab = function() {
};
maxwell.Splitter.prototype.animateShow = function() {
};
maxwell.Splitter.prototype.animateHide = function() {
};
$namespace("maxwell.Splitter.Resize");
maxwell.Splitter.Resize = function(_6bd, _6be, _6bf) {
	maxwell.Splitter.Resize.superclass.constructor.call(this, _6bd, _6be, _6bf);
	var deid = $id(this.node.firstChild);
	this.dd = new YAHOO.util.DD($id(this.node), "splitter");
	this.dd.setOuterHandleElId(deid);
	var self = this;
	this.dd.onDrag = function(_6c2) {
		self.resizeAB();
	};
	this.dd.endDrag = function() {
		for ( var j in YAHOO.util.DragDropMgr.ids["splitter"]) {
			var dd = YAHOO.util.DragDropMgr.ids["splitter"][j];
			dd.resetConstraints(false);
		}
		YAHOO.util.DragDropMgr.refreshCache({
			splitter : true
		});
	};
	this.parentNode = this.node.parentNode;
	YAHOO.util.Event.on(window, "resize", this.resizeAS, this, true);
};
$extend(maxwell.Splitter.Resize, maxwell.Splitter);
maxwell.Splitter.Resize.prototype.resizeAB = function() {
};
maxwell.Splitter.Resize.prototype.resizeAS = function() {
};
maxwell.Splitter.Resize.prototype.createShowAnim = function() {
};
maxwell.Splitter.Resize.prototype.createHideAnim = function() {
};
maxwell.Splitter.Resize.prototype.animateShow = function() {
	var anim = this.createShowAnim();
	anim.onComplete.subscribe(function(type, args, me) {
		this.animating = false;
		this.resizeAS();
		YAHOO.util.Dom.addClass(this.node.firstChild.firstChild, "makemin");
		YAHOO.util.Dom.removeClass(this.node.firstChild.firstChild, "makemax");
		this.dd.endDrag();
	}, this, true);
	anim.onTween.subscribe(function() {
		this.resizeAS();
	}, this, true);
	anim.animate();
};
maxwell.Splitter.Resize.prototype.animateHide = function() {
	var anim = this.createHideAnim();
	anim.onComplete.subscribe(function(type, args, me) {
		this.animating = false;
		this.resizeAS();
		YAHOO.util.Dom.removeClass(this.node.firstChild.firstChild, "makemin");
		YAHOO.util.Dom.addClass(this.node.firstChild.firstChild, "makemax");
		this.dd.endDrag();
	}, this, true);
	anim.onTween.subscribe(function() {
		this.resizeAS();
	}, this, true);
	anim.animate();
};
$namespace("maxwell.Splitter.TopBottom");
maxwell.Splitter.TopBottom = function(_6cd, _6ce, _6cf) {
	this.nodeBadjust = null;
	var _6d0 = {
		className : "topbottom"
	};
	if (_6cf) {
		_6d0.className += " " + _6cf;
	}
	sn(_6cd, null, {
		position : "absolute",
		top : "0px",
		bottom : "auto",
		width : "100%",
		display : "block",
		zoom : "1",
		height : "75%"
	});
	sn(_6ce, null, {
		position : "absolute",
		top : "auto",
		bottom : "0px",
		width : "100%",
		display : "block",
		zoom : "1"
	});
	maxwell.Splitter.TopBottom.superclass.constructor.call(this, _6cd, _6ce,
			_6d0);
	this.dd.setXConstraint(0, 0);
	YAHOO.util.Event.onAvailable(this.node, function() {
		this.resizeAB();
	}, this, true);
};
$extend(maxwell.Splitter.TopBottom, maxwell.Splitter.Resize);
maxwell.Splitter.TopBottom.prototype.calcPadding = function() {
	if (this.nodeBadjust == null) {
		this.nodeBadjust = 0;
		var pt = YAHOO.util.Dom.getStyle(this.nodeB, "paddingTop");
		if (pt && $endsWith(pt, "px")) {
			this.nodeBadjust += parseInt(pt.substring(0, pt.length - 2));
		}
		var pb = YAHOO.util.Dom.getStyle(this.nodeB, "paddingBottom");
		if (pb && $endsWith(pb, "px")) {
			this.nodeBadjust += parseInt(pb.substring(0, pb.length - 2));
		}
	}
};
maxwell.Splitter.TopBottom.prototype.resizeAB = function() {
	var top = this.node.offsetTop;
	var _6d4 = this.parentNode.offsetHeight;
	var _6d5 = _6d4 - top - this.node.offsetHeight;
	this.calcPadding();
	_6d5 = _6d5 - this.nodeBadjust;
	if (_6d5 < 0) {
		_6d5 = 0;
	}
	if (top < 0) {
		top = 0;
	}
	this.nodeA.style.height = top + "px";
	this.nodeB.style.height = _6d5 + "px";
	this.node.style.left = "0px";
};
maxwell.Splitter.TopBottom.prototype.resizeAS = function() {
	var _6d6 = this.parentNode.offsetHeight;
	var top = _6d6 - this.nodeB.offsetHeight - this.node.offsetHeight;
	if (top < 0) {
		top = 0;
	}
	this.node.style.top = top + "px";
	this.nodeA.style.height = top + "px";
	this.node.style.left = "0px";
};
maxwell.Splitter.TopBottom.prototype.createTab = function() {
	return cn("div", {
		className : "sliderhoriz"
	}, null, "<div class='horizbutton makemin'></div>");
};
maxwell.Splitter.TopBottom.prototype.createShowAnim = function() {
	if (this.config.minHeight && this.height < this.config.minHeight) {
		this.height = this.config.minHeight;
	}
	return new YAHOO.util.Anim(this.nodeB, {
		height : {
			to : this.height
		}
	}, 0.2, YAHOO.util.Easing.easeOut);
};
maxwell.Splitter.TopBottom.prototype.createHideAnim = function() {
	this.height = this.nodeB.offsetHeight;
	return new YAHOO.util.Anim(this.nodeB, {
		height : {
			to : 4
		}
	}, 0.2, YAHOO.util.Easing.easeIn);
};
$namespace("maxwell.Splitter.LeftRight");
maxwell.Splitter.LeftRight = function(_6d8, _6d9) {
	var _6da = {
		className : "leftright"
	};
	sn(_6d8, null, {
		position : "absolute",
		left : "0px",
		right : "auto",
		height : "100%"
	});
	sn(_6d9, null, {
		position : "absolute",
		left : "auto",
		right : "0px",
		height : "100%"
	});
	maxwell.Splitter.LeftRight.superclass.constructor.call(this, _6d8, _6d9,
			_6da);
	this.dd.setYConstraint(0, 0);
	YAHOO.util.Event.onAvailable(this.node, function() {
		this.resizeAB();
	}, this, true);
};
$extend(maxwell.Splitter.LeftRight, maxwell.Splitter.Resize);
maxwell.Splitter.LeftRight.prototype.resizeAB = function() {
	var left = this.node.offsetLeft;
	var _6dc = this.node.parentNode.offsetWidth;
	var _6dd = _6dc - left - this.node.offsetWidth;
	this.nodeA.style.width = left + "px";
	this.nodeB.style.width = _6dd + "px";
};
maxwell.Splitter.LeftRight.prototype.resizeAS = function() {
	var _6de = this.node.parentNode.offsetWidth;
	var left = this.nodeA.offsetWidth;
	var _6e0 = _6de - left - this.node.offsetWidth;
	this.node.style.left = left + "px";
	this.nodeB.style.width = _6e0 + "px";
};
maxwell.Splitter.LeftRight.prototype.createTab = function() {
	return cn("div", {
		className : "slidervert"
	}, null, "<div class='vertbutton makemin'></div>");
};
maxwell.Splitter.LeftRight.prototype.createShowAnim = function() {
	if (this.config.minWidth && this.width < this.config.minWidth) {
		this.width = this.config.minWidth;
	}
	return new YAHOO.util.Anim(this.nodeA, {
		width : {
			to : this.width
		}
	}, 0.2, YAHOO.util.Easing.easeOut);
};
maxwell.Splitter.LeftRight.prototype.createHideAnim = function() {
	this.width = this.nodeA.offsetWidth;
	return new YAHOO.util.Anim(this.nodeA, {
		width : {
			to : 4
		}
	}, 0.2, YAHOO.util.Easing.easeIn);
};
$namespace("maxwell.Toolbar");
maxwell.Toolbar = function() {
	this.node = cn(
			"div",
			{
				className : "editbuttons"
			},
			null,
			"<ul class=\"leftset\"></ul><ul id=\"engine\" style=\"display:block;width:300px;margin:0 auto\"></ul><ul class=\"rightset\"></ul>");
	this.onupdate = new YAHOO.util.CustomEvent("onupdate", this, true);
	var _6e1 = this.node.firstChild;
	var _6e2 = this.node.lastChild;
	var _6e3 = this.node.firstChild.nextSibling;
	var la = maxwell.Toolbar.createSmallButton("Layout");
	var ex = maxwell.Toolbar.createSmallButton("Expand All");
	var co = maxwell.Toolbar.createSmallButton("Collapse All");
	var _6e7 = cn(
			"li",
			{
				style : "padding-top:4px"
			},
			null,
			"This is running the V2 engine. <a class=\"changeEngine\" id=\"downgrade\">Downgrade to V1</a>");
	var _6e8 = cn(
			"li",
			{
				style : "padding-top:4px"
			},
			null,
			"This is running the V1 engine. <a class=\"changeEngine\" id=\"upgrade\">Upgrade to V2</a>");
	_6e1.appendChild(la);
	_6e1.appendChild(ex);
	_6e1.appendChild(co);
	if (isV2) {
		_6e3.appendChild(_6e7);
	} else {
		_6e3.appendChild(_6e8);
	}
	var bb = cn("li", {
		className : "goback"
	}, null, "<a href=\"" + profileurl + "\">Back to My Pipes</a>");
	var sa = maxwell.Toolbar.createBigButton("Save");
	var pu = maxwell.Toolbar.createBigButton("Properties&hellip;");
	var cl = maxwell.Toolbar.createBigButton("Save a copy");
	var ne = maxwell.Toolbar.createBigButton("New");
	_6e2.appendChild(bb);
	_6e2.appendChild(ne);
	_6e2.appendChild(sa);
	_6e2.appendChild(cl);
	_6e2.appendChild(pu);
	YAHOO.util.Event.addListener("downgrade", "click", this.showEngineMsg,
			true, this);
	YAHOO.util.Event.addListener("upgrade", "click", this.showEngineMsg, true,
			this);
	YAHOO.util.Event.addListener(la, "click", this.layout, true, this);
	YAHOO.util.Event.addListener(ex, "click", this.expand, true, this);
	YAHOO.util.Event.addListener(co, "click", this.collapse, true, this);
	YAHOO.util.Event.addListener(sa, "click", this.save, true, this);
	YAHOO.util.Event.addListener(pu, "click", this.publish, true, this);
	YAHOO.util.Event.addListener(cl, "click", this.clone, true, this);
	YAHOO.util.Event.addListener(ne, "click", this.neweditor, true, this);
	this.savebutton = sa;
	this.publishbutton = pu;
	this.clonebutton = cl;
};
maxwell.Toolbar.createSmallButton = function(text) {
	return cn(
			"li",
			null,
			null,
			"<div class='menusmallbutton'>"
					+ text
					+ "<div class='menusmallbuttonleft'></div><div class='menusmallbuttonright'></div></div>");
};
maxwell.Toolbar.createBigButton = function(text) {
	return cn(
			"li",
			null,
			null,
			"<div class='menubigbutton'>"
					+ text
					+ "<div class='menubigbuttonleft'></div><div class='menubigbuttonright'></div></div>");
};
maxwell.Toolbar.prototype.neweditor = function() {
	window.location.href = newurl;
};
maxwell.Toolbar.prototype.updateInfo = function(type, args, self) {
	if (this.editor) {
		this.editor.onload.unsubscribe(this.oneditorload, this, true);
		this.editor.onsave.unsubscribe(this.oneditorsave, this, true);
		this.editor.onclone.unsubscribe(this.oneditorclone, this, true);
		this.editor.onloadstart.unsubscribe(this.oneditorloadstart, this, true);
		this.editor.onsavestart.unsubscribe(this.oneditorsavestart, this, true);
		this.editor.onclonestart.unsubscribe(this.oneditorclonestart, this,
				true);
		this.editor.onchange.unsubscribe(this.oneditorchange, this, true);
	}
	var _6f3 = args[0];
	this.update(_6f3);
	this.editor = _6f3;
	this.editor.onload.subscribe(this.oneditorload, this, true);
	this.editor.onsave.subscribe(this.oneditorsave, this, true);
	this.editor.onclone.subscribe(this.oneditorclone, this, true);
	this.editor.onsavestart.subscribe(this.oneditorsavestart, this, true);
	this.editor.onloadstart.subscribe(this.oneditorloadstart, this, true);
	this.editor.onclonestart.subscribe(this.oneditorclonestart, this, true);
	this.editor.onchange.subscribe(this.oneditorchange, this, true);
};
maxwell.Toolbar.prototype.oneditorchange = function() {
	YAHOO.util.Dom.removeClass(this.savebutton, "disabled");
	this.onupdate.fire(maxwell.PipesEditor.CurrentEditor());
};
maxwell.Toolbar.prototype.layout = function() {
	maxwell.PipesEditor.CurrentEditor().layout.layout([ 25, 25 ]);
};
maxwell.Toolbar.prototype.save = function() {
	var _6f4 = maxwell.PipesEditor.CurrentEditor();
	if (_6f4.dirty) {
		_6f4.save();
	}
};
maxwell.Toolbar.prototype.showEngineMsg = function(e) {
	new maxwell.EngineMsg(e);
};
maxwell.Toolbar.prototype.publish = function() {
	var _6f6 = maxwell.PipesEditor.CurrentEditor();
	new maxwell.PropertiesDialog(maxwell.PipesEditor.CurrentEditor());
};
maxwell.Toolbar.prototype.clone = function() {
	var _6f7 = maxwell.PipesEditor.CurrentEditor();
	if (!_6f7.pipe.id) {
		return;
	}
	_6f7.pipe.name = "Copy of " + _6f7.pipe.name;
	maxwell.PipesEditor.CurrentEditor().clone();
};
maxwell.Toolbar.prototype.maintainhash = function() {
	var _6f8 = maxwell.PipesEditor.CurrentEditor();
	if (maxwell.PipesEditor._instance.editorIndex == 0) {
		if (!maxwell.Util.ua.isSafari) {
			if (_6f8.pipe.id != this.oldid) {
				window.location.hash = _6f8.pipe.id;
			}
		}
	}
	this.oldid = _6f8.pipe.id;
};
maxwell.Toolbar.prototype.oneditorclone = function(type, args, self) {
	if (this.cloning) {
		this.cloning.remove();
		this.cloning = null;
	}
	if (!args[0]) {
		new maxwell.Dialog.Alert({
			body : args[1],
			content : "Problem cloning"
		});
		return;
	}
	this.maintainhash();
	this.update(maxwell.PipesEditor.CurrentEditor());
	maxwell.PipesEditor.SetRunPipeStatus("Pipe Cloned");
};
maxwell.Toolbar.prototype.oneditorsave = function(type, args, self) {
	var _6ff = maxwell.PipesEditor.CurrentEditor();
	if (this.saving) {
		this.saving.remove();
		this.saving = null;
	}
	if (!args[0]) {
		new maxwell.Dialog.Alert({
			body : args[1],
			content : "Problem saving"
		});
		return;
	}
	this.maintainhash();
	this.update(_6ff);
	maxwell.PipesEditor.SetRunPipeStatus("Pipe Saved");
};
maxwell.Toolbar.prototype.oneditorload = function(type, args, self) {
	var _703 = maxwell.PipesEditor.CurrentEditor();
	if (this.loading) {
		this.loading.remove();
		this.loading = null;
	}
	if (!args[0]) {
		new maxwell.Dialog.Alert({
			body : args[1],
			content : "Problem loading"
		});
		return;
	}
	this.update(_703);
	maxwell.PipesEditor.SetRunPipeStatus("Pipe Loaded");
};
maxwell.Toolbar.prototype.update = function(_704) {
	if (!_704) {
		_704 = maxwell.PipesEditor.CurrentEditor();
	}
	maxwell.PipesEditor.UpdatePipeName();
	if (_704.pipe.name) {
		document.title = "Pipes: editing '" + _704.pipe.name + "'";
	} else {
		document.title = "Pipes: editing pipe";
	}
	if (_704.dirty) {
		YAHOO.util.Dom.removeClass(this.savebutton, "disabled");
	} else {
		YAHOO.util.Dom.addClass(this.savebutton, "disabled");
	}
	if (_704.pipe.is_owner === 1
			&& window.location.search.slice(1).indexOf("_v2=") == -1) {
		YAHOO.util.Dom
				.setStyle(
						YAHOO.util.Dom.getElementsByClassName("changeEngine")[0],
						"display", "inline");
	} else {
		YAHOO.util.Dom.setStyle(YAHOO.util.Dom
				.getElementsByClassName("changeEngine")[0], "display", "none");
	}
	if (_704.pipe.is_owner) {
		this.savebutton.style.display = "inline";
		this.publishbutton.style.display = "inline";
	} else {
		this.savebutton.style.display = "none";
		this.publishbutton.style.display = "none";
	}
	if (_704.pipe.id) {
		this.clonebutton.style.display = "inline";
		YAHOO.util.Dom.removeClass(this.publishbutton, "disabled");
	} else {
		this.clonebutton.style.display = "none";
		YAHOO.util.Dom.addClass(this.publishbutton, "disabled");
	}
	this.onupdate.fire(_704);
};
maxwell.Toolbar.prototype.oneditorsavestart = function(type, args, self) {
	var _708 = "<div class='dialogwaitmsg'>Saving&hellip;</div>";
	this.oldid = maxwell.PipesEditor.CurrentEditor().pipe.id;
	this.saving = new maxwell.Dialog({
		content : _708,
		className : "statusdialog"
	});
};
maxwell.Toolbar.prototype.oneditorloadstart = function(type, args, self) {
	var _70c = maxwell.PipesEditor.CurrentEditor().layout;
	if (_70c.invitearea) {
		_70c.invitearea.parentNode.removeChild(_70c.invitearea);
		_70c.invitearea = null;
	}
	var _70d = "<div class='dialogwaitmsg'>Loading&hellip;</div>";
	this.loading = new maxwell.Dialog({
		content : _70d,
		className : "statusdialog"
	});
};
maxwell.Toolbar.prototype.oneditorclonestart = function(type, args, self) {
	var _711 = "<div class='dialogwaitmsg'>Copying&hellip;</div>";
	this.oldid = maxwell.PipesEditor.CurrentEditor().pipe.id;
	this.cloning = new maxwell.Dialog({
		content : _711,
		className : "statusdialog"
	});
};
maxwell.Toolbar.prototype.oneditorremove = function(type, args, self) {
};
maxwell.Toolbar.prototype.preview = function() {
	maxwell.PipesEditor.CurrentEditor().preview();
};
maxwell.Toolbar.prototype.collapse = function() {
	maxwell.PipesEditor.CurrentEditor().collapseAll();
};
maxwell.Toolbar.prototype.expand = function() {
	maxwell.PipesEditor.CurrentEditor().expandAll();
};
$namespace("maxwell.InlineTextEdit");
maxwell.InlineTextEdit = function(node, _716) {
	this.onchange = new YAHOO.util.CustomEvent("onchange", this, true);
	this.originalNode = node;
	this.emptytext = _716;
	this.enable();
};
maxwell.InlineTextEdit.prototype.disable = function() {
	result = YAHOO.util.Event.removeListener(this.originalNode, "mouseover",
			this.inviteEditing);
	result = YAHOO.util.Event.removeListener(this.originalNode, "mouseout",
			this.hideEditing);
	result = YAHOO.util.Event.removeListener(this.originalNode, "click",
			this.startEditing);
	if (this.node) {
		result = YAHOO.util.Event.removeListener(this.input, "blur",
				this.checkBlur);
		result = YAHOO.util.Event.removeListener(this.input, "keydown",
				this.resize);
	}
};
maxwell.InlineTextEdit.prototype.enable = function() {
	YAHOO.util.Event.addListener(this.originalNode, "mouseover",
			this.inviteEditing, this, true);
	YAHOO.util.Event.addListener(this.originalNode, "mouseout",
			this.hideEditing, this, true);
	YAHOO.util.Event.addListener(this.originalNode, "click", this.startEditing,
			this, true);
	this.editing = false;
};
maxwell.InlineTextEdit.prototype.remove = function() {
	this.disable();
	if (this.node && this.node.parentNode) {
		this.node.parentNode.removeChild(this.node);
	}
};
maxwell.InlineTextEdit.prototype.resize = function(ev) {
	if (ev) {
		var key = YAHOO.util.Event.getCharCode(ev);
		if (key == 13) {
			this.inviteOk();
			return;
		}
	}
	var len = this.input.value.length;
	if (len < 5) {
		len = 5;
	}
	sn(this.input, {
		size : len
	});
};
maxwell.InlineTextEdit.prototype.init = function() {
	if (!this.node) {
		this.node = cn("div", {
			className : "inlineedit"
		}, null, "<input type=text>");
		this.buttons = cn("div", {
			className : "inlineeditbuttons"
		}, null, "<ul></ul>");
		this.ok = maxwell.Toolbar.createBigButton("OK");
		this.cancel = maxwell.Toolbar.createBigButton("Cancel");
		this.buttons.firstChild.appendChild(this.ok);
		this.buttons.firstChild.appendChild(this.cancel);
		this.node.appendChild(this.buttons);
		this.input = this.node.getElementsByTagName("input")[0];
		this.originalNode.parentNode.insertBefore(this.node, this.originalNode);
		this.originalNode.style.display = "none";
		YAHOO.util.Event.addListener(this.ok, "click", this.inviteOk, this,
				true);
		YAHOO.util.Event.addListener(this.cancel, "click", this.inviteCancel,
				this, true);
		YAHOO.util.Event.addListener(this.input, "blur", this.checkBlur, this,
				true);
		YAHOO.util.Event.addListener(this.input, "keydown", this.resize, this,
				true);
	}
};
maxwell.InlineTextEdit.prototype.inviteEditing = function(e) {
	if (!YAHOO.util.Dom.hasClass(this.originalNode, "invite")) {
		YAHOO.util.Dom.addClass(this.originalNode, "invite");
	}
};
maxwell.InlineTextEdit.prototype.hideEditing = function(e) {
	YAHOO.util.Dom.removeClass(this.originalNode, "invite");
};
maxwell.InlineTextEdit.prototype.startEditing = function(e) {
	this.init();
	var txt = this.originalNode.innerHTML;
	if (txt == this.emptytext) {
		txt = "";
	}
	this.input.value = uncook(txt);
	this.resize();
	this.node.style.display = "block";
	this.input.focus();
};
maxwell.InlineTextEdit.prototype.checkBlur = function(e) {
	var self = this;
	this.cancelBlur = window.setTimeout(function() {
		self.inviteOk();
	}, 200);
};
maxwell.InlineTextEdit.prototype.stopEditing = function(e) {
	this.inviteCancel();
};
maxwell.InlineTextEdit.prototype.inviteOk = function(e) {
	this.originalNode.innerHTML = this.input.value;
	this.onchange.fire(this.input.value);
	this.inviteCancel();
};
maxwell.InlineTextEdit.prototype.inviteCancel = function(e) {
	if (this.cancelBlur) {
		window.clearTimeout(this.cancelBlur);
		this.cancelBlur = null;
	}
	this.node.style.display = "none";
	this.originalNode.style.display = "inline";
};
$namespace("maxwell.PipesEditorTabs");
maxwell.PipesEditorTabs = function(_723) {
	this.onopeneditor = new YAHOO.util.CustomEvent("onopeneditor", this, true);
	this.oncloseeditor = new YAHOO.util.CustomEvent("oncloseeditor", this, true);
	this.onchangetitle = new YAHOO.util.CustomEvent("onchangetitle", this, true);
	if (!_723) {
		this.config = {};
	} else {
		this.config = _723;
	}
	this.node = cn(
			"div",
			{
				className : "pipetabs"
			},
			null,
			"<a href=\""
					+ profileurl
					+ "\"><img  src=\"http://l.yimg.com/a/i/us/pps/pipeslogo_editor.gif\" /></a>");
	this.tabsholder = cn("div", {
		className : "pipetabsholder"
	});
	this.tabs = cn("ul", {
		className : "pipetabsul"
	});
	this.tabsholder.appendChild(this.tabs);
	this.node.appendChild(this.tabsholder);
	this.shownTab = -1;
};
maxwell.PipesEditorTabs.prototype.updateTabText = function(_724, tab) {
	var _726 = this._indexOfEditor(_724);
	if (!tab) {
		tab = this.tabs.childNodes[_726];
	}
	var _727 = tab.getElementsByTagName("span")[0];
	var _728 = tab.getElementsByTagName("b")[0];
	var name = _724.pipe.name;
	if (!name || name.length == 0) {
		name = "untitled";
	}
	_727.innerHTML = cook(name);
	if (_724.dirty) {
		_728.style.display = "inline";
	} else {
		_728.style.display = "none";
	}
};
maxwell.PipesEditorTabs.prototype.addEditor = function(_72a) {
	var txt = "<div class='pipetabmain'><span></span><b class='pipetabdirty'>*</b><div class='pipetableft'></div><div class='pipetabright'></div></div>";
	var _72c = "pipetab";
	if (this.tabs.childNodes.length == 0) {
		_72c += " first only";
	} else {
		YAHOO.util.Dom.removeClass(this.tabs.childNodes[0], "only");
		YAHOO.util.Dom.removeClass(
				this.tabs.childNodes[this.tabs.childNodes.length - 1], "last");
		_72c += " last";
	}
	var node = cn("li", {
		className : _72c,
		editorid : $id(_72a.node)
	}, null, txt);
	this.tabs.appendChild(node);
	_72a.onload.subscribe(this.oneditorloadsave, this, true);
	_72a.onsave.subscribe(this.oneditorloadsave, this, true);
	_72a.onchange.subscribe(this.oneditorchange, this, true);
	_72a.onremove.subscribe(this.oneditorremove, this, true);
	this.updateTabText(_72a, node);
	YAHOO.util.Event.addListener($id(node.getElementsByTagName("span")[0]),
			"click", function(e) {
				this.openEditor(_72a);
				YAHOO.util.Event.stopEvent(e);
			}, this, true);
	return node;
};
maxwell.PipesEditorTabs.prototype.oneditorremove = function(type, args, me) {
	var _732 = args[0];
	this.removeEditor(_732);
};
maxwell.PipesEditorTabs.prototype.oneditorloadsave = function(type, args, me) {
	if (!args[0]) {
		return;
	}
	var _736 = args[2];
	this.updateTabText(_736);
};
maxwell.PipesEditorTabs.prototype.oneditorchange = function(type, args, me) {
	var _73a = args[2];
	this.updateTabText(_73a);
};
maxwell.PipesEditorTabs.prototype.getCurrentTab = function() {
	return this.tabs.childNodes[this.shownTab];
};
maxwell.PipesEditorTabs.prototype.closeEditor = function() {
	if (this.shownTab < 0) {
		return;
	}
	var tab = this.tabs.childNodes[this.shownTab];
	var _73c = this._editorAtIndex(this.shownTab);
	this.inline.remove();
	this.oncloseeditor.fire(_73c, tab);
	this.shownTab = -1;
};
maxwell.PipesEditorTabs.prototype.removeEditor = function(_73d) {
	_73d.onload.unsubscribe(this.oneditorloadsave, this, true);
	_73d.onsave.unsubscribe(this.oneditorloadsave, this, true);
	_73d.onchange.unsubscribe(this.oneditorchange, this, true);
	_73d.onremove.unsubscribe(this.oneditorremove, this, true);
	var _73e = this._indexOfEditor(_73d);
	var tab = this.tabs.childNodes[_73e];
	this.tabs.removeChild(tab);
	if (this.tabs.childNodes.length == 1) {
		YAHOO.util.Dom.addClass(this.tabs.childNodes[0], "only");
	} else {
		YAHOO.util.Dom.addClass(
				this.tabs.childNodes[this.tabs.childNodes.length - 1], "last");
	}
};
maxwell.PipesEditorTabs.prototype._indexOfEditor = function(_740) {
	var id = $id(_740.node);
	for ( var i = 0; i < this.tabs.childNodes.length; i++) {
		if (this.tabs.childNodes[i].getAttribute("editorid") == id) {
			return i;
		}
	}
	return -1;
};
maxwell.PipesEditorTabs.prototype._editorAtIndex = function(_743) {
	var id = this.tabs.childNodes[_743].getAttribute("editorid");
	var _745 = maxwell.PipesEditor.GetEditors();
	for ( var i = 0; i < _745.length; i++) {
		if ($id(_745[i].node) == id) {
			return _745[i];
		}
	}
	return null;
};
maxwell.PipesEditorTabs.prototype.openEditor = function(_747) {
	var _748 = this._indexOfEditor(_747);
	if (this.shownTab == _748) {
		return;
	}
	this.closeEditor();
	this.shownTab = _748;
	var tab = this.tabs.childNodes[this.shownTab];
	this.inline = new maxwell.InlineTextEdit(
			tab.getElementsByTagName("span")[0], "untitled");
	this.inline.onchange.subscribe(function(type, args, self) {
		this.onchangetitle.fire(_747, args[0]);
	}, this, true);
	this.onopeneditor.fire(_747, tab);
};
$namespace("maxwell.EngineMsg");
maxwell.EngineMsg = function(e) {
	var _74e = YAHOO.util.Event.getTarget(e).id;
	if (_74e == "downgrade") {
		var _74f = "Downgrade";
		var html = "<table cellspacing=0 cellpadding=0><tbody>";
		html += "<tr class='pipedesc'><td style='padding-top:12px'>The v1 engine will soon be replaced by v2. Any Pipe left running in v1 may cease to work after the v2 site wide auto upgrade. <p style='padding-top:5px'>Please report any <a target='_blank' href='http://tech.groups.yahoo.com/group/pipes-engine2users/post?subject=PipeID: "
				+ pipeID
				+ "&message=I am reverting Pipe ID: "
				+ pipeID
				+ " back to V1 because:'>issue(s)</a> you encountered in v2. By doing this you will help make v2 better.</p></td></tr>";
		html += "</tbody></table>";
	} else {
		var _74f = "Upgrade";
		var html = "<table cellspacing=0 cellpadding=0><tbody>";
		html += "<tr class='pipedesc'><td style='padding-top:12px;padding-bottom:10px'><p>We will be deprecating the current v1 engine soon. This is your chance to try your Pipe in v2 and report bugs.</p>  <p style='padding-top:5px'>You may encounter differences and incompatibilities when switching. (Find out more about <a target='_blank' href='http://blog.pipes.yahoo.net/?p=176'>v2</a>.)</p>  <p style='padding-top:5px'>Please take some time to log any questions or findings <a target='_blank' href='http://tech.groups.yahoo.com/group/pipes-engine2users/post?subject=PipeID: "
				+ pipeID
				+ "'>here</a>.</p> <p style='padding-top:5px'>Click on the Upgrade button to complete the process.</p> <p style='padding-top:5px'>Thanks for trying the v2 engine!</p></td></tr>";
		html += "</tbody></table>";
	}
	var self = this;
	var _752 = new maxwell.Dialog.OkCancel({
		className : "pipeinfotab",
		canceltext : "Cancel",
		oktext : _74f,
		body : null,
		content : html,
		onOk : function() {
			self.ok(_74e);
		}
	});
};
maxwell.EngineMsg.prototype.ok = function(id) {
	if (id == "downgrade") {
		window.location.href = downgradelink;
	} else {
		window.location.href = upgradelink;
	}
};
