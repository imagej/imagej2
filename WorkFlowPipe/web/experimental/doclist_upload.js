function d(a) {
	throw a;
}
var h = true, i = null, j = false, l, aa = aa || {}, n = this, ba = ".";
function ca(a) {
	a = a.split(ba);
	for ( var b = n, c; c = a.shift();)
		if (b[c] != i)
			b = b[c];
		else
			return i;
	return b
}
function p() {
}
function r(a) {
	a.O = function() {
		return a.Qn || (a.Qn = new a)
	}
}
var da = "object", ea = "[object Array]", fa = "number", ha = "splice", ia = "array", ja = "[object Function]", ka = "call", s = "function", la = "null";
function ma(a) {
	var b = typeof a;
	if (b == da)
		if (a) {
			if (a instanceof Array || !(a instanceof Object)
					&& Object.prototype.toString.call(a) == ea
					|| typeof a.length == fa && typeof a.splice != "undefined"
					&& typeof a.propertyIsEnumerable != "undefined"
					&& !a.propertyIsEnumerable(ha))
				return ia;
			if (!(a instanceof Object)
					&& (Object.prototype.toString.call(a) == ja || typeof a.call != "undefined"
							&& typeof a.propertyIsEnumerable != "undefined"
							&& !a.propertyIsEnumerable(ka)))
				return s
		} else
			return la;
	else if (b == s && typeof a.call == "undefined")
		return da;
	return b
}
function t(a) {
	return ma(a) == ia
}
function na(a) {
	var b = ma(a);
	return b == ia || b == da && typeof a.length == fa
}
var oa = "string";
function u(a) {
	return typeof a == oa
}
function pa(a) {
	return ma(a) == s
}
function qa(a) {
	a = ma(a);
	return a == da || a == ia || a == s
}
function ra(a) {
	return a[sa] || (a[sa] = ++ta)
}
var sa = "closure_uid_" + Math.floor(Math.random() * 2147483648).toString(36), ta = 0;
function ua(a) {
	return a.call.apply(a.bind, arguments)
}
function va(a, b) {
	var c = b || n;
	if (arguments.length > 2) {
		var e = Array.prototype.slice.call(arguments, 2);
		return function() {
			var f = Array.prototype.slice.call(arguments);
			Array.prototype.unshift.apply(f, e);
			return a.apply(c, f)
		}
	} else
		return function() {
			return a.apply(c, arguments)
		}
}
var wa = "native code";
function w() {
	w = Function.prototype.bind
			&& Function.prototype.bind.toString().indexOf(wa) != -1 ? ua : va;
	return w.apply(i, arguments)
}
function xa(a) {
	var b = Array.prototype.slice.call(arguments, 1);
	return function() {
		var c = Array.prototype.slice.call(arguments);
		c.unshift.apply(c, b);
		return a.apply(this, c)
	}
}
var ya = Date.now || function() {
	return +new Date
};
function z(a, b) {
	function c() {
	}
	c.prototype = b.prototype;
	a.a = b.prototype;
	a.prototype = new c;
	a.prototype.constructor = a
}
Function.prototype.bind = Function.prototype.bind || function(a) {
	if (arguments.length > 1) {
		var b = Array.prototype.slice.call(arguments, 1);
		b.unshift(this, a);
		return w.apply(i, b)
	} else
		return w(this, a)
};
function za(a, b) {
	var c = a.length - b.length;
	return c >= 0 && a.indexOf(b, c) == c
}
var A = "";
function Aa(a) {
	return /^[\s\xa0]*$/.test(a == i ? A : String(a))
}
function Ba(a) {
	return a.replace(/^[\s\xa0]+|[\s\xa0]+$/g, A)
}
var Da = /^[a-zA-Z0-9\-_.!~*'()]*$/;
function Ea(a) {
	a = String(a);
	if (!Da.test(a))
		return encodeURIComponent(a);
	return a
}
var Fa = "&", Ga = "&amp;", Ha = "<", Ia = "&lt;", Ja = ">", Ka = "&gt;", La = '"', Ma = "&quot;";
function Na(a) {
	if (!Oa.test(a))
		return a;
	if (a.indexOf(Fa) != -1)
		a = a.replace(Pa, Ga);
	if (a.indexOf(Ha) != -1)
		a = a.replace(Qa, Ia);
	if (a.indexOf(Ja) != -1)
		a = a.replace(Ra, Ka);
	if (a.indexOf(La) != -1)
		a = a.replace(Sa, Ma);
	return a
}
var Pa = /&/g, Qa = /</g, Ra = />/g, Sa = /\"/g, Oa = /[&<>\"]/, Ta = "amp", Ua = "lt", Va = "gt", Wa = "quot", Xa = "#", Ya = "0";
function Za(a) {
	return a.replace(/&([^;]+);/g, function(b, c) {
		switch (c) {
		case Ta:
			return Fa;
		case Ua:
			return Ha;
		case Va:
			return Ja;
		case Wa:
			return La;
		default:
			if (c.charAt(0) == Xa) {
				var e = Number(Ya + c.substr(1));
				if (!isNaN(e))
					return String.fromCharCode(e)
			}
			return b
		}
	})
}
function $a() {
	return Array.prototype.join.call(arguments, A)
}
var ab = "(\\d*)(\\D*)", bb = "g";
function cb(a, b) {
	for ( var c = 0, e = Ba(String(a)).split(ba), f = Ba(String(b)).split(ba), g = Math
			.max(e.length, f.length), k = 0; c == 0 && k < g; k++) {
		var m = e[k] || A, o = f[k] || A, q = RegExp(ab, bb), x = RegExp(ab, bb);
		do {
			var v = q.exec(m) || [ A, A, A ], y = x.exec(o) || [ A, A, A ];
			if (v[0].length == 0 && y[0].length == 0)
				break;
			c = db(v[1].length == 0 ? 0 : parseInt(v[1], 10),
					y[1].length == 0 ? 0 : parseInt(y[1], 10))
					|| db(v[2].length == 0, y[2].length == 0) || db(v[2], y[2])
		} while (c == 0)
	}
	return c
}
function db(a, b) {
	if (a < b)
		return -1;
	else if (a > b)
		return 1;
	return 0
};
var B = Array.prototype, eb = B.indexOf ? function(a, b, c) {
	return B.indexOf.call(a, b, c)
} : function(a, b, c) {
	c = c == i ? 0 : c < 0 ? Math.max(0, a.length + c) : c;
	if (u(a)) {
		if (!u(b) || b.length != 1)
			return -1;
		return a.indexOf(b, c)
	}
	for (c = c; c < a.length; c++)
		if (c in a && a[c] === b)
			return c;
	return -1
}, C = B.forEach ? function(a, b, c) {
	B.forEach.call(a, b, c)
} : function(a, b, c) {
	for ( var e = a.length, f = u(a) ? a.split(A) : a, g = 0; g < e; g++)
		g in f && b.call(c, f[g], g, a)
}, fb = B.filter ? function(a, b, c) {
	return B.filter.call(a, b, c)
}
		: function(a, b, c) {
			for ( var e = a.length, f = [], g = 0, k = u(a) ? a.split(A) : a, m = 0; m < e; m++)
				if (m in k) {
					var o = k[m];
					if (b.call(c, o, m, a))
						f[g++] = o
				}
			return f
		}, gb = B.map ? function(a, b, c) {
	return B.map.call(a, b, c)
}
		: function(a, b, c) {
			for ( var e = a.length, f = Array(e), g = u(a) ? a.split(A) : a, k = 0; k < e; k++)
				if (k in g)
					f[k] = b.call(c, g[k], k, a);
			return f
		}, hb = B.some ? function(a, b, c) {
	return B.some.call(a, b, c)
} : function(a, b, c) {
	for ( var e = a.length, f = u(a) ? a.split(A) : a, g = 0; g < e; g++)
		if (g in f && b.call(c, f[g], g, a))
			return h;
	return j
}, ib = B.every ? function(a, b, c) {
	return B.every.call(a, b, c)
} : function(a, b, c) {
	for ( var e = a.length, f = u(a) ? a.split(A) : a, g = 0; g < e; g++)
		if (g in f && !b.call(c, f[g], g, a))
			return j;
	return h
};
function jb(a, b) {
	var c;
	a: {
		c = a.length;
		for ( var e = u(a) ? a.split(A) : a, f = 0; f < c; f++)
			if (f in e && b.call(void 0, e[f], f, a)) {
				c = f;
				break a
			}
		c = -1
	}
	return c < 0 ? i : u(a) ? a.charAt(c) : a[c]
}
function kb(a, b) {
	return eb(a, b) >= 0
}
function mb(a, b) {
	var c = eb(a, b), e;
	if (e = c >= 0)
		B.splice.call(a, c, 1);
	return e
}
function nb() {
	return B.concat.apply(B, arguments)
}
function ob(a) {
	if (t(a))
		return nb(a);
	else {
		for ( var b = [], c = 0, e = a.length; c < e; c++)
			b[c] = a[c];
		return b
	}
}
var pb = "callee";
function qb(a) {
	for ( var b = 1; b < arguments.length; b++) {
		var c = arguments[b], e;
		if (t(c) || (e = na(c)) && c.hasOwnProperty(pb))
			a.push.apply(a, c);
		else if (e)
			for ( var f = a.length, g = c.length, k = 0; k < g; k++)
				a[f + k] = c[k];
		else
			a.push(c)
	}
}
function rb(a) {
	return B.splice.apply(a, sb(arguments, 1))
}
function sb(a, b, c) {
	return arguments.length <= 2 ? B.slice.call(a, b) : B.slice.call(a, b, c)
};
var tb = "StopIteration" in n ? n.StopIteration : Error("StopIteration");
function ub() {
}
ub.prototype.next = function() {
	d(tb)
};
ub.prototype.ed = function() {
	return this
};
function vb(a) {
	if (a instanceof ub)
		return a;
	if (typeof a.ed == s)
		return a.ed(j);
	if (na(a)) {
		var b = 0, c = new ub;
		c.next = function() {
			for (;;) {
				if (b >= a.length)
					d(tb);
				if (b in a)
					return a[b++];
				else
					b++
			}
		};
		return c
	}
	d(Error("Not implemented"))
}
function wb(a, b, c) {
	if (na(a))
		try {
			C(a, b, c)
		} catch (e) {
			if (e !== tb)
				d(e)
		}
	else {
		a = vb(a);
		try {
			for (;;)
				b.call(c, a.next(), undefined, a)
		} catch (f) {
			if (f !== tb)
				d(f)
		}
	}
};
function xb(a, b) {
	for ( var c in a)
		b.call(void 0, a[c], c, a)
}
function yb(a, b) {
	var c = {}, e;
	for (e in a)
		if (b.call(void 0, a[e], e, a))
			c[e] = a[e];
	return c
}
function zb(a) {
	var b = 0, c;
	for (c in a)
		b++;
	return b
}
function Ab(a) {
	var b = [], c = 0, e;
	for (e in a)
		b[c++] = a[e];
	return b
}
function Bb(a) {
	var b = [], c = 0, e;
	for (e in a)
		b[c++] = e;
	return b
}
function Cb(a, b) {
	for ( var c in a)
		if (a[c] == b)
			return h;
	return j
}
function Db(a) {
	for ( var b in a)
		return j;
	return h
}
function Eb(a, b) {
	var c;
	if (c = b in a)
		delete a[b];
	return c
}
function Fb(a, b, c) {
	if (b in a)
		d(Error('The object already contains the key "' + b + La));
	a[b] = c
}
var Gb = [ "constructor", "hasOwnProperty", "isPrototypeOf",
		"propertyIsEnumerable", "toLocaleString", "toString", "valueOf" ];
function Hb(a) {
	for ( var b, c, e = 1; e < arguments.length; e++) {
		c = arguments[e];
		for (b in c)
			a[b] = c[b];
		for ( var f = 0; f < Gb.length; f++) {
			b = Gb[f];
			if (Object.prototype.hasOwnProperty.call(c, b))
				a[b] = c[b]
		}
	}
}
var Ib = "Uneven number of arguments";
function Jb() {
	var a = arguments.length;
	if (a == 1 && t(arguments[0]))
		return Jb.apply(i, arguments[0]);
	if (a % 2)
		d(Error(Ib));
	for ( var b = {}, c = 0; c < a; c += 2)
		b[arguments[c]] = arguments[c + 1];
	return b
};
function Kb(a) {
	if (typeof a.eb == s)
		return a.eb();
	if (u(a))
		return a.split(A);
	if (na(a)) {
		for ( var b = [], c = a.length, e = 0; e < c; e++)
			b.push(a[e]);
		return b
	}
	return Ab(a)
}
function Lb(a) {
	if (typeof a.wb == s)
		return a.wb();
	if (typeof a.eb != s) {
		if (na(a) || u(a)) {
			var b = [];
			a = a.length;
			for ( var c = 0; c < a; c++)
				b.push(c);
			return b
		}
		return Bb(a)
	}
}
function Mb(a, b, c) {
	if (typeof a.forEach == s)
		a.forEach(b, c);
	else if (na(a) || u(a))
		C(a, b, c);
	else
		for ( var e = Lb(a), f = Kb(a), g = f.length, k = 0; k < g; k++)
			b.call(c, f[k], e && e[k], a)
};
function D(a) {
	this.S = {};
	this.H = [];
	var b = arguments.length;
	if (b > 1) {
		if (b % 2)
			d(Error(Ib));
		for ( var c = 0; c < b; c += 2)
			this.t(arguments[c], arguments[c + 1])
	} else
		a && this.Ig(a)
}
l = D.prototype;
l.K = 0;
l.mf = 0;
l.Rb = function() {
	return this.K
};
l.eb = function() {
	Nb(this);
	for ( var a = [], b = 0; b < this.H.length; b++)
		a.push(this.S[this.H[b]]);
	return a
};
l.wb = function() {
	Nb(this);
	return this.H.concat()
};
l.Oa = function(a) {
	return Ob(this.S, a)
};
l.rb = function() {
	return this.K == 0
};
l.clear = function() {
	this.S = {};
	this.mf = this.K = this.H.length = 0
};
l.remove = function(a) {
	if (Ob(this.S, a)) {
		delete this.S[a];
		this.K--;
		this.mf++;
		this.H.length > 2 * this.K && Nb(this);
		return h
	}
	return j
};
function Nb(a) {
	if (a.K != a.H.length) {
		for ( var b = 0, c = 0; b < a.H.length;) {
			var e = a.H[b];
			if (Ob(a.S, e))
				a.H[c++] = e;
			b++
		}
		a.H.length = c
	}
	if (a.K != a.H.length) {
		var f = {};
		for (c = b = 0; b < a.H.length;) {
			e = a.H[b];
			if (!Ob(f, e)) {
				a.H[c++] = e;
				f[e] = 1
			}
			b++
		}
		a.H.length = c
	}
}
l.w = function(a, b) {
	if (Ob(this.S, a))
		return this.S[a];
	return b
};
l.t = function(a, b) {
	if (!Ob(this.S, a)) {
		this.K++;
		this.H.push(a);
		this.mf++
	}
	this.S[a] = b
};
l.Ig = function(a) {
	var b;
	if (a instanceof D) {
		b = a.wb();
		a = a.eb()
	} else {
		b = Bb(a);
		a = Ab(a)
	}
	for ( var c = 0; c < b.length; c++)
		this.t(b[c], a[c])
};
l.va = function() {
	return new D(this)
};
l.ed = function(a) {
	Nb(this);
	var b = 0, c = this.H, e = this.S, f = this.mf, g = this, k = new ub;
	k.next = function() {
		for (;;) {
			if (f != g.mf)
				d(Error("The map has changed since the iterator was created"));
			if (b >= c.length)
				d(tb);
			var m = c[b++];
			return a ? m : e[m]
		}
	};
	return k
};
function Ob(a, b) {
	return Object.prototype.hasOwnProperty.call(a, b)
};
function Pb(a) {
	this.S = new D;
	a && this.Ig(a)
}
var Qb = "o";
function Rb(a) {
	var b = typeof a;
	return b == da && a || b == s ? Qb + ra(a) : b.substr(0, 1) + a
}
l = Pb.prototype;
l.Rb = function() {
	return this.S.Rb()
};
l.add = function(a) {
	this.S.t(Rb(a), a)
};
l.Ig = function(a) {
	a = Kb(a);
	for ( var b = a.length, c = 0; c < b; c++)
		this.add(a[c])
};
l.Pd = function(a) {
	a = Kb(a);
	for ( var b = a.length, c = 0; c < b; c++)
		this.remove(a[c])
};
l.remove = function(a) {
	return this.S.remove(Rb(a))
};
l.clear = function() {
	this.S.clear()
};
l.rb = function() {
	return this.S.rb()
};
l.contains = function(a) {
	return this.S.Oa(Rb(a))
};
l.sk = function(a) {
	var b = new Pb;
	a = Kb(a);
	for ( var c = 0; c < a.length; c++) {
		var e = a[c];
		this.contains(e) && b.add(e)
	}
	return b
};
l.eb = function() {
	return this.S.eb()
};
l.va = function() {
	return new Pb(this)
};
l.ed = function() {
	return this.S.ed(j)
};
var Sb = "message";
function Tb(a, b) {
	var c = typeof a == oa ? Error(a) : a;
	if (!c.stack)
		c.stack = Ub(arguments.callee.caller);
	if (b) {
		for ( var e = 0; c[Sb + e];)
			++e;
		c[Sb + e] = String(b)
	}
	return c
}
function Ub(a) {
	return Vb(a || arguments.callee.caller, [])
}
var Wb = "[...circular reference...]", Xb = "(", Yb = ", ", $b = "boolean", ac = "true", bc = "false", cc = "[fn]", dc = "...", ec = ")\n", fc = "[exception trying to get caller]\n", gc = "[...long stack...]", hc = "[end]";
function Vb(a, b) {
	var c = [];
	if (kb(b, a))
		c.push(Wb);
	else if (a && b.length < 50) {
		c.push(ic(a) + Xb);
		for ( var e = a.arguments, f = 0; f < e.length; f++) {
			f > 0 && c.push(Yb);
			var g;
			g = e[f];
			switch (typeof g) {
			case da:
				g = g ? da : la;
				break;
			case oa:
				g = g;
				break;
			case fa:
				g = String(g);
				break;
			case $b:
				g = g ? ac : bc;
				break;
			case s:
				g = (g = ic(g)) ? g : cc;
				break;
			default:
				g = typeof g
			}
			if (g.length > 40)
				g = g.substr(0, 40) + dc;
			c.push(g)
		}
		b.push(a);
		c.push(ec);
		try {
			c.push(Vb(a.caller, b))
		} catch (k) {
			c.push(fc)
		}
	} else
		a ? c.push(gc) : c.push(hc);
	return c.join(A)
}
var jc = "[Anonymous]";
function ic(a) {
	a = String(a);
	if (!kc[a]) {
		var b = /function ([^\(]+)/.exec(a);
		kc[a] = b ? b[1] : jc
	}
	return kc[a]
}
var kc = {};
function lc(a, b) {
	this.x = a !== undefined ? a : 0;
	this.y = b !== undefined ? b : 0
}
lc.prototype.va = function() {
	return new lc(this.x, this.y)
};
function mc(a, b) {
	return new lc(a.x - b.x, a.y - b.y)
};
function nc(a, b) {
	this.width = a;
	this.height = b
}
nc.prototype.va = function() {
	return new nc(this.width, this.height)
};
nc.prototype.rb = function() {
	return !(this.width * this.height)
};
nc.prototype.floor = function() {
	this.width = Math.floor(this.width);
	this.height = Math.floor(this.height);
	return this
};
nc.prototype.round = function() {
	this.width = Math.round(this.width);
	this.height = Math.round(this.height);
	return this
};
var oc, pc, qc, rc, sc, tc, uc, vc;
function wc() {
	return n.navigator ? n.navigator.userAgent : i
}
function xc() {
	return n.navigator
}
sc = rc = qc = pc = oc = j;
var yc;
if (yc = wc()) {
	var zc = xc();
	oc = yc.indexOf("Opera") == 0;
	pc = !oc && yc.indexOf("MSIE") != -1;
	rc = (qc = !oc && yc.indexOf("WebKit") != -1) && yc.indexOf("Mobile") != -1;
	sc = !oc && !qc && zc.product == "Gecko"
}
var Ac = oc, E = pc, F = sc, G = qc, Bc = rc, Cc = xc(), Dc = Cc && Cc.platform
		|| A;
tc = Dc.indexOf("Mac") != -1;
uc = Dc.indexOf("Win") != -1;
vc = Dc.indexOf("Linux") != -1;
var Ec = !!xc() && (xc().appVersion || A).indexOf("X11") != -1, Fc;
a: {
	var Gc = A, Hc;
	if (Ac && n.opera) {
		var Ic = n.opera.version;
		Gc = typeof Ic == s ? Ic() : Ic
	} else {
		if (F)
			Hc = /rv\:([^\);]+)(\)|;)/;
		else if (E)
			Hc = /MSIE\s+([^\);]+)(\)|;)/;
		else if (G)
			Hc = /WebKit\/(\S+)/;
		if (Hc) {
			var Jc = Hc.exec(wc());
			Gc = Jc ? Jc[1] : A
		}
	}
	if (E) {
		var Kc, Lc = n.document;
		Kc = Lc ? Lc.documentMode : undefined;
		if (Kc > parseFloat(Gc)) {
			Fc = String(Kc);
			break a
		}
	}
	Fc = Gc
}
var Mc = Fc, Nc = {};
function H(a) {
	return Nc[a] || (Nc[a] = cb(Mc, a) >= 0)
};
var Oc, Pc = "9", Qc = !E || H(Pc), Rc = E && !H(Pc);
function Sc(a) {
	return (a = a.className) && typeof a.split == s ? a.split(/\s+/) : []
}
var I = " ";
function J(a) {
	var b = Sc(a), c;
	c = sb(arguments, 1);
	for ( var e = 0, f = 0; f < c.length; f++)
		if (!kb(b, c[f])) {
			b.push(c[f]);
			e++
		}
	c = e == c.length;
	a.className = b.join(I);
	return c
}
function Tc(a) {
	var b = Sc(a), c;
	c = sb(arguments, 1);
	for ( var e = 0, f = 0; f < b.length; f++)
		if (kb(c, b[f])) {
			rb(b, f--, 1);
			e++
		}
	c = e == c.length;
	a.className = b.join(I);
	return c
};
function K(a) {
	return a ? new Uc(Vc(a)) : Oc || (Oc = new Uc)
}
function Wc(a) {
	return u(a) ? document.getElementById(a) : a
}
function Xc(a, b, c) {
	return Yc(document, a, b, c)
}
var Zc = "*", $c = "528";
function Yc(a, b, c, e) {
	a = e || a;
	b = b && b != Zc ? b.toUpperCase() : A;
	if (a.querySelectorAll && a.querySelector && (!G || ad(document) || H($c))
			&& (b || c))
		return a.querySelectorAll(b + (c ? ba + c : A));
	if (c && a.getElementsByClassName) {
		a = a.getElementsByClassName(c);
		if (b) {
			e = {};
			for ( var f = 0, g = 0, k; k = a[g]; g++)
				if (b == k.nodeName)
					e[f++] = k;
			e.length = f;
			return e
		} else
			return a
	}
	a = a.getElementsByTagName(b || Zc);
	if (c) {
		e = {};
		for (g = f = 0; k = a[g]; g++) {
			b = k.className;
			if (typeof b.split == s && kb(b.split(/\s+/), c))
				e[f++] = k
		}
		e.length = f;
		return e
	} else
		return a
}
var bd = "style", cd = "class", dd = "for";
function ed(a, b) {
	xb(b, function(c, e) {
		if (e == bd)
			a.style.cssText = c;
		else if (e == cd)
			a.className = c;
		else if (e == dd)
			a.htmlFor = c;
		else if (e in fd)
			a.setAttribute(fd[e], c);
		else
			a[e] = c
	})
}
var gd = "height", hd = "width", id = "type", fd = {
	cellpadding : "cellPadding",
	cellspacing : "cellSpacing",
	colspan : "colSpan",
	rowspan : "rowSpan",
	valign : "vAlign",
	height : gd,
	width : hd,
	usemap : "useMap",
	frameborder : "frameBorder",
	type : id
}, jd = "500", kd = "9.50";
function ld(a) {
	var b = a.document;
	if (G && !H(jd) && !Bc) {
		if (typeof a.innerHeight == "undefined")
			a = window;
		b = a.innerHeight;
		var c = a.document.documentElement.scrollHeight;
		if (a == a.top)
			if (c < b)
				b -= 15;
		return new nc(a.innerWidth, b)
	}
	a = ad(b);
	if (Ac && !H(kd))
		a = j;
	a = a ? b.documentElement : b.body;
	return new nc(a.clientWidth, a.clientHeight)
}
function md(a) {
	return a ? a.parentWindow || a.defaultView : window
}
function nd() {
	return od(document, arguments)
}
var pd = ' name="', qd = ' type="';
function od(a, b) {
	var c = b[0], e = b[1];
	if (!Qc && e && (e.name || e.type)) {
		c = [ Ha, c ];
		e.name && c.push(pd, Na(e.name), La);
		if (e.type) {
			c.push(qd, Na(e.type), La);
			var f = {};
			Hb(f, e);
			e = f;
			delete e.type
		}
		c.push(Ja);
		c = c.join(A)
	}
	c = a.createElement(c);
	if (e)
		if (u(e))
			c.className = e;
		else
			t(e) ? J.apply(i, [ c ].concat(e)) : ed(c, e);
	b.length > 2 && rd(a, c, b, 2);
	return c
}
function rd(a, b, c, e) {
	function f(k) {
		if (k)
			b.appendChild(u(k) ? a.createTextNode(k) : k)
	}
	for (e = e; e < c.length; e++) {
		var g = c[e];
		na(g) && !(qa(g) && g.nodeType > 0) ? C(sd(g) ? ob(g) : g, f) : f(g)
	}
}
var L = "div", td = "<br>";
function ud(a, b) {
	var c = a.createElement(L);
	if (E) {
		c.innerHTML = td + b;
		c.removeChild(c.firstChild)
	} else
		c.innerHTML = b;
	if (c.childNodes.length == 1)
		return c.removeChild(c.firstChild);
	else {
		for ( var e = a.createDocumentFragment(); c.firstChild;)
			e.appendChild(c.firstChild);
		return e
	}
}
var vd = "CSS1Compat";
function ad(a) {
	return a.compatMode == vd
}
function wd(a) {
	for ( var b; b = a.firstChild;)
		a.removeChild(b)
}
function xd(a, b) {
	b.parentNode && b.parentNode.insertBefore(a, b.nextSibling)
}
function yd(a) {
	return a && a.parentNode ? a.parentNode.removeChild(a) : i
}
function zd(a) {
	return Ad(a.firstChild)
}
function Ad(a) {
	for (; a && a.nodeType != 1;)
		a = a.nextSibling;
	return a
}
function Bd(a, b) {
	if (a.contains && b.nodeType == 1)
		return a == b || a.contains(b);
	if (typeof a.compareDocumentPosition != "undefined")
		return a == b || Boolean(a.compareDocumentPosition(b) & 16);
	for (; b && a != b;)
		b = b.parentNode;
	return b == a
}
function Vc(a) {
	return a.nodeType == 9 ? a : a.ownerDocument || a.document
}
function Cd(a) {
	return G ? a.document || a.contentWindow.document : a.contentDocument
			|| a.contentWindow.document
}
var Dd = "textContent";
function Ed(a, b) {
	if (Dd in a)
		a.textContent = b;
	else if (a.firstChild && a.firstChild.nodeType == 3) {
		for (; a.lastChild != a.firstChild;)
			a.removeChild(a.lastChild);
		a.firstChild.data = b
	} else {
		wd(a);
		a.appendChild(Vc(a).createTextNode(b))
	}
}
var Fd = {
	SCRIPT : 1,
	STYLE : 1,
	HEAD : 1,
	IFRAME : 1,
	OBJECT : 1
}, Gd = "\n", Hd = {
	IMG : I,
	BR : Gd
}, Id = "tabindex";
function Jd(a) {
	var b = a.getAttributeNode(Id);
	if (b && b.specified) {
		a = a.tabIndex;
		return typeof a == fa && a >= 0
	}
	return j
}
var Kd = "innerText";
function Ld(a) {
	if (Rc && Kd in a)
		a = a.innerText.replace(/(\r\n|\r|\n)/g, Gd);
	else {
		var b = [];
		Md(a, b, h);
		a = b.join(A)
	}
	a = a.replace(/ \xAD /g, I).replace(/\xAD/g, A);
	E || (a = a.replace(/ +/g, I));
	if (a != I)
		a = a.replace(/^\s*/, A);
	return a
}
function Md(a, b, c) {
	if (!(a.nodeName in Fd))
		if (a.nodeType == 3)
			c ? b.push(String(a.nodeValue).replace(/(\r\n|\r|\n)/g, A)) : b
					.push(a.nodeValue);
		else if (a.nodeName in Hd)
			b.push(Hd[a.nodeName]);
		else
			for (a = a.firstChild; a;) {
				Md(a, b, c);
				a = a.nextSibling
			}
}
function sd(a) {
	if (a && typeof a.length == fa)
		if (qa(a))
			return typeof a.item == s || typeof a.item == oa;
		else if (pa(a))
			return typeof a.item == s;
	return j
}
function Uc(a) {
	this.D = a || n.document || document
}
l = Uc.prototype;
l.g = K;
function Nd(a) {
	return a.D
}
l.b = function(a) {
	return u(a) ? this.D.getElementById(a) : a
};
function Od(a, b, c, e) {
	return Yc(a.D, b, c, e)
}
l.d = function() {
	return od(this.D, arguments)
};
l.createElement = function(a) {
	return this.D.createElement(a)
};
l.createTextNode = function(a) {
	return this.D.createTextNode(a)
};
function Pd(a) {
	a = !G && ad(a.D) ? a.D.documentElement : a.D.body;
	return new lc(a.scrollLeft, a.scrollTop)
}
l.appendChild = function(a, b) {
	a.appendChild(b)
};
l.append = function(a) {
	rd(Vc(a), a, arguments, 1)
};
l.Sh = xd;
l.removeNode = yd;
l.Cd = zd;
l.contains = Bd;
function Qd() {
}
Qd.prototype.ah = j;
Qd.prototype.u = function() {
	if (!this.ah) {
		this.ah = h;
		this.h()
	}
};
Qd.prototype.h = function() {
};
var Rd;
var Sd = (Rd = "ScriptEngine" in n && n.ScriptEngine() == "JScript") ? n
		.ScriptEngineMajorVersion()
		+ ba + n.ScriptEngineMinorVersion() + ba + n.ScriptEngineBuildVersion()
		: Ya;
function M(a) {
	this.ba = Rd ? [] : A;
	a != i && this.append.apply(this, arguments)
}
M.prototype.t = function(a) {
	this.clear();
	this.append(a)
};
if (Rd) {
	M.prototype.Qg = 0;
	M.prototype.append = function(a, b) {
		if (b == i)
			this.ba[this.Qg++] = a;
		else {
			this.ba.push.apply(this.ba, arguments);
			this.Qg = this.ba.length
		}
		return this
	}
} else
	M.prototype.append = function(a, b) {
		this.ba += a;
		if (b != i)
			for ( var c = 1; c < arguments.length; c++)
				this.ba += arguments[c];
		return this
	};
M.prototype.clear = function() {
	if (Rd)
		this.Qg = this.ba.length = 0;
	else
		this.ba = A
};
M.prototype.toString = function() {
	if (Rd) {
		var a = this.ba.join(A);
		this.clear();
		a && this.append(a);
		return a
	} else
		return this.ba
};
var Td = {
	"\u0000" : "&#0;",
	'"' : Ma,
	"&" : Ga,
	"'" : "&#39;",
	"<" : Ia,
	">" : Ka,
	"\t" : "&#9;",
	"\n" : "&#10;",
	"\u000b" : "&#11;",
	"\u000c" : "&#12;",
	"\r" : "&#13;",
	" " : "&#32;",
	"-" : "&#45;",
	"/" : "&#47;",
	"=" : "&#61;",
	"`" : "&#96;",
	"\u0085" : "&#133;",
	"\u00a0" : "&#160;",
	"\u2028" : "&#8232;",
	"\u2029" : "&#8233;"
};
function Ud(a) {
	return Td[a]
}
var Vd = /[\x00\x22\x26\x27\x3c\x3e]/g;
function Wd(a) {
	return String(a).replace(Vd, Ud)
};
var Xd = "@", Yd = "]", Zd = ")";
function $d(a) {
	a = String(a);
	var b;
	b = /^\s*$/.test(a) ? j
			: /^[\],:{}\s\u2028\u2029]*$/
					.test(a
							.replace(/\\["\\\/bfnrtu]/g, Xd)
							.replace(
									/"[^"\\\n\r\u2028\u2029\x00-\x08\x10-\x1f\x80-\x9f]*"|true|false|null|-?\d+(?:\.\d*)?(?:[eE][+\-]?\d+)?/g,
									Yd).replace(
									/(?:^|:|,)(?:[\s\u2028\u2029]*\[)+/g, A));
	if (b)
		try {
			return eval(Xb + a + Zd)
		} catch (c) {
		}
	d(Error("Invalid JSON string: " + a))
}
function ae(a) {
	return eval(Xb + a + Zd)
}
function be(a) {
	var b = [];
	ce(new de, a, b);
	return b.join(A)
}
function de() {
}
var ee = "[", fe = ",", ge = "{", he = ":", ie = "}";
function ce(a, b, c) {
	switch (typeof b) {
	case oa:
		je(a, b, c);
		break;
	case fa:
		c.push(isFinite(b) && !isNaN(b) ? b : la);
		break;
	case $b:
		c.push(b);
		break;
	case "undefined":
		c.push(la);
		break;
	case da:
		if (b == i) {
			c.push(la);
			break
		}
		if (t(b)) {
			var e = b.length;
			c.push(ee);
			for ( var f = A, g = 0; g < e; g++) {
				c.push(f);
				ce(a, b[g], c);
				f = fe
			}
			c.push(Yd);
			break
		}
		c.push(ge);
		e = A;
		for (f in b)
			if (b.hasOwnProperty(f)) {
				g = b[f];
				if (typeof g != s) {
					c.push(e);
					je(a, f, c);
					c.push(he);
					ce(a, g, c);
					e = fe
				}
			}
		c.push(ie);
		break;
	case s:
		break;
	default:
		d(Error("Unknown type: " + typeof b))
	}
}
var ke = {
	'"' : '\\"',
	"\\" : "\\\\",
	"/" : "\\/",
	"\u0008" : "\\b",
	"\u000c" : "\\f",
	"\n" : "\\n",
	"\r" : "\\r",
	"\t" : "\\t",
	"\u000b" : "\\u000b"
}, le = /\uffff/.test("\uffff") ? /[\\\"\x00-\x1f\x7f-\uffff]/g
		: /[\\\"\x00-\x1f\x7f-\xff]/g, me = "\\u", ne = "000", oe = "00";
function je(a, b, c) {
	c.push(La, b.replace(le, function(e) {
		if (e in ke)
			return ke[e];
		var f = e.charCodeAt(0), g = me;
		if (f < 16)
			g += ne;
		else if (f < 256)
			g += oe;
		else if (f < 4096)
			g += Ya;
		return ke[e] = g + f.toString(16)
	}), La)
};
var pe = "a", qe = new Function(pe, "return a");
var re, se = !E || H(Pc), te = "8", ue = E && !H(te);
function N(a, b) {
	this.type = a;
	this.currentTarget = this.target = b
}
z(N, Qd);
l = N.prototype;
l.h = function() {
	delete this.type;
	delete this.target;
	delete this.currentTarget
};
l.Uc = j;
l.Ve = h;
l.stopPropagation = function() {
	this.Uc = h
};
l.preventDefault = function() {
	this.Ve = j
};
function ve(a) {
	a.preventDefault()
};
function we(a, b) {
	a && this.Ge(a, b)
}
z(we, N);
var xe = [ 1, 4, 2 ];
l = we.prototype;
l.target = i;
l.relatedTarget = i;
l.offsetX = 0;
l.offsetY = 0;
l.clientX = 0;
l.clientY = 0;
l.screenX = 0;
l.screenY = 0;
l.button = 0;
l.keyCode = 0;
l.charCode = 0;
l.ctrlKey = j;
l.altKey = j;
l.shiftKey = j;
l.metaKey = j;
l.Ro = j;
l.Ja = i;
var ye = "mouseover", ze = "mouseout", Ae = "keypress";
l.Ge = function(a, b) {
	var c = this.type = a.type;
	this.target = a.target || a.srcElement;
	this.currentTarget = b;
	var e = a.relatedTarget;
	if (e) {
		if (F)
			try {
				qe(e.nodeName)
			} catch (f) {
				e = i
			}
	} else if (c == ye)
		e = a.fromElement;
	else if (c == ze)
		e = a.toElement;
	this.relatedTarget = e;
	this.offsetX = a.offsetX !== undefined ? a.offsetX : a.layerX;
	this.offsetY = a.offsetY !== undefined ? a.offsetY : a.layerY;
	this.clientX = a.clientX !== undefined ? a.clientX : a.pageX;
	this.clientY = a.clientY !== undefined ? a.clientY : a.pageY;
	this.screenX = a.screenX || 0;
	this.screenY = a.screenY || 0;
	this.button = a.button;
	this.keyCode = a.keyCode || 0;
	this.charCode = a.charCode || (c == Ae ? a.keyCode : 0);
	this.ctrlKey = a.ctrlKey;
	this.altKey = a.altKey;
	this.shiftKey = a.shiftKey;
	this.metaKey = a.metaKey;
	this.Ro = tc ? a.metaKey : a.ctrlKey;
	this.state = a.state;
	this.Ja = a;
	delete this.Ve;
	delete this.Uc
};
var Be = "click";
function Ce(a, b) {
	return se ? a.Ja.button == b : a.type == Be ? b == 0
			: !!(a.Ja.button & xe[b])
}
l.stopPropagation = function() {
	we.a.stopPropagation.call(this);
	if (this.Ja.stopPropagation)
		this.Ja.stopPropagation();
	else
		this.Ja.cancelBubble = h
};
l.preventDefault = function() {
	we.a.preventDefault.call(this);
	var a = this.Ja;
	if (a.preventDefault)
		a.preventDefault();
	else {
		a.returnValue = j;
		if (ue)
			try {
				if (a.ctrlKey || a.keyCode >= 112 && a.keyCode <= 123)
					a.keyCode = -1
			} catch (b) {
			}
	}
};
l.h = function() {
	we.a.h.call(this);
	this.relatedTarget = this.currentTarget = this.target = this.Ja = i
};
function De(a, b) {
	this.Ak = b;
	this.Ic = [];
	if (a > this.Ak)
		d(Error("[goog.structs.SimplePool] Initial cannot be greater than max"));
	for ( var c = 0; c < a; c++)
		this.Ic.push(this.Mb ? this.Mb() : {})
}
z(De, Qd);
De.prototype.Mb = i;
De.prototype.Gj = i;
function Ee(a) {
	if (a.Ic.length)
		return a.Ic.pop();
	return a.Mb ? a.Mb() : {}
}
function Fe(a, b) {
	a.Ic.length < a.Ak ? a.Ic.push(b) : Ge(a, b)
}
function Ge(a, b) {
	if (a.Gj)
		a.Gj(b);
	else if (qa(b))
		if (pa(b.u))
			b.u();
		else
			for ( var c in b)
				delete b[c]
}
De.prototype.h = function() {
	De.a.h.call(this);
	for ( var a = this.Ic; a.length;)
		Ge(this, a.pop());
	delete this.Ic
};
function He() {
}
var Ie = 0;
l = He.prototype;
l.key = 0;
l.Rd = j;
l.kj = j;
var Je = "Invalid listener argument";
l.Ge = function(a, b, c, e, f, g) {
	if (pa(a))
		this.uk = h;
	else if (a && a.handleEvent && pa(a.handleEvent))
		this.uk = j;
	else
		d(Error(Je));
	this.Qc = a;
	this.Xk = b;
	this.src = c;
	this.type = e;
	this.capture = !!f;
	this.$f = g;
	this.kj = j;
	this.key = ++Ie;
	this.Rd = j
};
l.handleEvent = function(a) {
	if (this.uk)
		return this.Qc.call(this.$f || this.src, a);
	return this.Qc.handleEvent.call(this.Qc, a)
};
var Ke, Le, Me, Ne, Oe, Pe, Qe, Re, Se, Te, Ue, Ve = "5.7";
(function() {
	function a() {
		return {
			K : 0,
			tb : 0
		}
	}
	function b() {
		return []
	}
	function c() {
		function y(ga) {
			return k.call(y.src, y.key, ga)
		}
		return y
	}
	function e() {
		return new He
	}
	function f() {
		return new we
	}
	var g = Rd && !(cb(Sd, Ve) >= 0), k;
	Pe = function(y) {
		k = y
	};
	if (g) {
		Ke = function() {
			return Ee(m)
		};
		Le = function(y) {
			Fe(m, y)
		};
		Me = function() {
			return Ee(o)
		};
		Ne = function(y) {
			Fe(o, y)
		};
		Oe = function() {
			return Ee(q)
		};
		Qe = function() {
			Fe(q, c())
		};
		Re = function() {
			return Ee(x)
		};
		Se = function(y) {
			Fe(x, y)
		};
		Te = function() {
			return Ee(v)
		};
		Ue = function(y) {
			Fe(v, y)
		};
		var m = new De(0, 600);
		m.Mb = a;
		var o = new De(0, 600);
		o.Mb = b;
		var q = new De(0, 600);
		q.Mb = c;
		var x = new De(0, 600);
		x.Mb = e;
		var v = new De(0, 600);
		v.Mb = f
	} else {
		Ke = a;
		Le = p;
		Me = b;
		Ne = p;
		Oe = c;
		Qe = p;
		Re = e;
		Se = p;
		Te = f;
		Ue = p
	}
})();
var We = {}, Xe = {}, Ye = {}, Ze = {};
function O(a, b, c, e, f) {
	if (b)
		if (t(b)) {
			for ( var g = 0; g < b.length; g++)
				O(a, b[g], c, e, f);
			return i
		} else {
			e = !!e;
			var k = Xe;
			b in k || (k[b] = Ke());
			k = k[b];
			if (!(e in k)) {
				k[e] = Ke();
				k.K++
			}
			k = k[e];
			var m = ra(a), o;
			k.tb++;
			if (k[m]) {
				o = k[m];
				for (g = 0; g < o.length; g++) {
					k = o[g];
					if (k.Qc == c && k.$f == f) {
						if (k.Rd)
							break;
						return o[g].key
					}
				}
			} else {
				o = k[m] = Me();
				k.K++
			}
			g = Oe();
			g.src = a;
			k = Re();
			k.Ge(c, g, a, b, e, f);
			c = k.key;
			g.key = c;
			o.push(k);
			We[c] = k;
			Ye[m] || (Ye[m] = Me());
			Ye[m].push(k);
			if (a.addEventListener) {
				if (a == n || !a.Dj)
					a.addEventListener(b, g, e)
			} else
				a.attachEvent($e(b), g);
			return c
		}
	else
		d(Error("Invalid event type"))
}
function af(a, b, c, e, f) {
	if (t(b)) {
		for ( var g = 0; g < b.length; g++)
			af(a, b[g], c, e, f);
		return i
	}
	e = !!e;
	a = bf(a, b, e);
	if (!a)
		return j;
	for (g = 0; g < a.length; g++)
		if (a[g].Qc == c && a[g].capture == e && a[g].$f == f)
			return cf(a[g].key);
	return j
}
function cf(a) {
	if (!We[a])
		return j;
	var b = We[a];
	if (b.Rd)
		return j;
	var c = b.src, e = b.type, f = b.Xk, g = b.capture;
	if (c.removeEventListener) {
		if (c == n || !c.Dj)
			c.removeEventListener(e, f, g)
	} else
		c.detachEvent && c.detachEvent($e(e), f);
	c = ra(c);
	f = Xe[e][g][c];
	if (Ye[c]) {
		var k = Ye[c];
		mb(k, b);
		k.length == 0 && delete Ye[c]
	}
	b.Rd = h;
	f.Ek = h;
	df(e, g, c, f);
	delete We[a];
	return h
}
function df(a, b, c, e) {
	if (!e.ig)
		if (e.Ek) {
			for ( var f = 0, g = 0; f < e.length; f++)
				if (e[f].Rd) {
					var k = e[f].Xk;
					k.src = i;
					Qe(k);
					Se(e[f])
				} else {
					if (f != g)
						e[g] = e[f];
					g++
				}
			e.length = g;
			e.Ek = j;
			if (g == 0) {
				Ne(e);
				delete Xe[a][b][c];
				Xe[a][b].K--;
				if (Xe[a][b].K == 0) {
					Le(Xe[a][b]);
					delete Xe[a][b];
					Xe[a].K--
				}
				if (Xe[a].K == 0) {
					Le(Xe[a]);
					delete Xe[a]
				}
			}
		}
}
function ef(a) {
	var b, c = 0, e = b == i;
	b = !!b;
	if (a == i)
		xb(Ye, function(k) {
			for ( var m = k.length - 1; m >= 0; m--) {
				var o = k[m];
				if (e || b == o.capture) {
					cf(o.key);
					c++
				}
			}
		});
	else {
		a = ra(a);
		if (Ye[a]) {
			a = Ye[a];
			for ( var f = a.length - 1; f >= 0; f--) {
				var g = a[f];
				if (e || b == g.capture) {
					cf(g.key);
					c++
				}
			}
		}
	}
	return c
}
function bf(a, b, c) {
	var e = Xe;
	if (b in e) {
		e = e[b];
		if (c in e) {
			e = e[c];
			a = ra(a);
			if (e[a])
				return e[a]
		}
	}
	return i
}
var gf = "on";
function $e(a) {
	if (a in Ze)
		return Ze[a];
	return Ze[a] = gf + a
}
function hf(a, b, c, e, f) {
	var g = 1;
	b = ra(b);
	if (a[b]) {
		a.tb--;
		a = a[b];
		if (a.ig)
			a.ig++;
		else
			a.ig = 1;
		try {
			for ( var k = a.length, m = 0; m < k; m++) {
				var o = a[m];
				if (o && !o.Rd)
					g &= jf(o, f) !== j
			}
		} finally {
			a.ig--;
			df(c, e, b, a)
		}
	}
	return Boolean(g)
}
function jf(a, b) {
	var c = a.handleEvent(b);
	a.kj && cf(a.key);
	return c
}
var kf = "window.event";
Pe(function(a, b) {
	if (!We[a])
		return h;
	var c = We[a], e = c.type, f = Xe;
	if (!(e in f))
		return h;
	f = f[e];
	var g, k;
	if (re === undefined)
		re = E && !n.addEventListener;
	if (re) {
		g = b || ca(kf);
		var m = h in f, o = j in f;
		if (m) {
			if (g.keyCode < 0 || g.returnValue != undefined)
				return h;
			a: {
				var q = j;
				if (g.keyCode == 0)
					try {
						g.keyCode = -1;
						break a
					} catch (x) {
						q = h
					}
				if (q || g.returnValue == undefined)
					g.returnValue = h
			}
		}
		q = Te();
		q.Ge(g, this);
		g = h;
		try {
			if (m) {
				for ( var v = Me(), y = q.currentTarget; y; y = y.parentNode)
					v.push(y);
				k = f[h];
				k.tb = k.K;
				for ( var ga = v.length - 1; !q.Uc && ga >= 0 && k.tb; ga--) {
					q.currentTarget = v[ga];
					g &= hf(k, v[ga], e, h, q)
				}
				if (o) {
					k = f[j];
					k.tb = k.K;
					for (ga = 0; !q.Uc && ga < v.length && k.tb; ga++) {
						q.currentTarget = v[ga];
						g &= hf(k, v[ga], e, j, q)
					}
				}
			} else
				g = jf(c, q)
		} finally {
			if (v) {
				v.length = 0;
				Ne(v)
			}
			q.u();
			Ue(q)
		}
		return g
	}
	e = new we(b, this);
	try {
		g = jf(c, e)
	} finally {
		e.u()
	}
	return g
});
function Q() {
}
z(Q, Qd);
l = Q.prototype;
l.Dj = h;
l.ui = i;
l.uh = function() {
	return this.ui
};
l.Ii = function(a) {
	this.ui = a
};
l.addEventListener = function(a, b, c, e) {
	O(this, a, b, c, e)
};
l.removeEventListener = function(a, b, c, e) {
	af(this, a, b, c, e)
};
l.dispatchEvent = function(a) {
	a = a;
	if (u(a))
		a = new N(a, this);
	else if (a instanceof N)
		a.target = a.target || this;
	else {
		var b = a;
		a = new N(a.type, this);
		Hb(a, b)
	}
	b = 1;
	var c, e = a.type, f = Xe;
	if (e in f) {
		f = f[e];
		e = h in f;
		var g;
		if (e) {
			c = [];
			for (g = this; g; g = g.uh())
				c.push(g);
			g = f[h];
			g.tb = g.K;
			for ( var k = c.length - 1; !a.Uc && k >= 0 && g.tb; k--) {
				a.currentTarget = c[k];
				b &= hf(g, c[k], a.type, h, a) && a.Ve != j
			}
		}
		if (j in f) {
			g = f[j];
			g.tb = g.K;
			if (e)
				for (k = 0; !a.Uc && k < c.length && g.tb; k++) {
					a.currentTarget = c[k];
					b &= hf(g, c[k], a.type, j, a) && a.Ve != j
				}
			else
				for (c = this; !a.Uc && c && g.tb; c = c.uh()) {
					a.currentTarget = c;
					b &= hf(g, c, a.type, j, a) && a.Ve != j
				}
		}
		a = Boolean(b)
	} else
		a = h;
	return a
};
l.h = function() {
	Q.a.h.call(this);
	ef(this);
	this.ui = i
};
var R = "/";
function lf(a, b, c, e) {
	this.na = mf++;
	this.fa = a || A;
	this.Na = b || i;
	this.Kb = 0;
	this.Ck = new D;
	this.Am = this.fm = this.hm = this.Cl = this.ie = i;
	this.yi = j;
	if (c && za(c, R))
		c = c.substring(0, c.length - 1);
	this.Vc = c;
	this.xc = e;
	this.Cf = i
}
z(lf, Q);
var nf = "default", of = "inqueue", pf = "start", qf = "transfer", rf = "pause", sf = "backoff", S = "success", T = "error", tf = "partial-error", uf = "cancel", vf = {
	bj : nf,
	Pl : of,
	Tl : pf,
	Hg : qf,
	Rl : rf,
	aj : sf,
	Ul : S,
	Nl : T,
	Ql : tf,
	Ml : uf
}, mf = 0;
lf.prototype.k = function() {
	return String(this.na)
};
function wf(a) {
	return a.xc
}
lf.prototype.getName = function() {
	var a = this.fa.split(R).pop();
	return this.Vc ? this.Vc + R + a : a
};
lf.prototype.Wd = function(a) {
	this.fa = a
};
function xf(a) {
	return a.Na
}
var yf = "File(id:", zf = ", path:", Af = ", relativeDirectoryPath:", Bf = ", selectionId:", Cf = ", bytesTransferred:", Df = ", bytesTotal:";
lf.prototype.toString = function() {
	return [ yf, this.na, zf, this.fa, Af, this.Vc, Bf, this.xc, Cf, this.Kb,
			Df, this.Na, Zd ].join(A)
};
var Ef = "noerror";
function Ff(a) {
	this.Hf = a;
	this.Wa = A;
	this.ya = Ef;
	this.sg = this.Ab = A
}
z(Ff, Q);
l = Ff.prototype;
l.dispatchEvent = function(a) {
	try {
		return Ff.a.dispatchEvent.call(this, a)
	} catch (b) {
	}
};
l.Da = function() {
	return this.Hf
};
l.Rf = function() {
	return this.Ab
};
l.Sf = function() {
	return this.Wa
};
l.xb = function() {
	return this.sg
};
l.send = function(a) {
	this.Wa = a;
	this.yc(a)
};
var Gf = "serverinvalidresponse", Hf = "Invalid response from server.", If = "serverrejected", Jf = "Server rejected upload.";
l.Lc = function(a) {
	this.sg = a || A;
	var b;
	try {
		b = ae(this.sg)
	} catch (c) {
		this.handleError(Gf, Hf);
		return
	}
	if (b.sessionStatus != undefined) {
		this.Hf.Na != i && Kf(this, this.Hf.Na);
		this.dispatchEvent(S)
	} else
		b.errorMessage != undefined ? this.handleError(If, Jf) : this
				.handleError(Gf, Hf)
};
l.handleError = function(a, b) {
	this.ya = a;
	this.Ab = u(b) ? b : b.message;
	this.dispatchEvent(T)
};
var Lf = "progress";
function Kf(a, b) {
	if (b)
		a.Hf.Kb = b;
	a.dispatchEvent(Lf)
};
var Mf = "There was a client error with the upload.";
function Nf(a, b) {
	Ff.call(this, a);
	this.ra = b;
	this.kf = j;
	this.Ue = i;
	this.nj = Mf;
	this.oo = 5242880
}
z(Nf, Ff);
var Of = "abort";
Nf.prototype.abort = function() {
	if (this.kf) {
		this.ra.abortUpload(wf(this.Da()));
		window.clearInterval(this.Ue)
	}
	this.dispatchEvent(Of)
};
var Pf = "SUCCESS", Qf = "INVALID_URL", Rf = "exception", Sf = "A URL with an invalid domain was detected: ", Tf = "UNKNOWN_ERROR", Uf = "MISSING_SELECTION";
Nf.prototype.yc = function(a) {
	switch (String(this.ra.startUpload(a, wf(this.Da()), this.oo))) {
	case Pf:
		this.Ue = window.setInterval(w(Nf.prototype.vp, this), 50);
		this.dispatchEvent(pf);
		this.kf = h;
		break;
	case Qf:
		this.handleError(Rf, Sf + a);
		break;
	case Tf:
	case Uf:
		this.handleError(Rf, this.nj)
	}
};
var Vf = "unknownerror", Wf = "Unable to obtain file upload progress from java plugin.", Xf = "COMPLETE", Yf = "IN_PROGRESS", Zf = "CLIENT_ERROR";
Nf.prototype.vp = function() {
	var a = this.Da(), b = $d(this.ra.getProgress(a.xc));
	if (!b) {
		window.clearTimeout(this.Ue);
		this.handleError(Vf, Wf);
		this.ra.freeResources(a.xc)
	}
	var c = b.bytesTransfered;
	switch (b.state) {
	case Xf:
		window.clearTimeout(this.Ue);
		Kf(this, c);
		this.Lc(this.ra.readServerResponse(a.xc));
		this.ra.freeResources(a.xc);
		break;
	case Yf:
		Kf(this, c);
		break;
	case Zf:
		window.clearTimeout(this.Ue);
		this.handleError(Rf, this.nj);
		this.ra.freeResources(a.xc)
	}
};
function $f(a, b, c, e) {
	this.top = a;
	this.right = b;
	this.bottom = c;
	this.left = e
}
$f.prototype.va = function() {
	return new $f(this.top, this.right, this.bottom, this.left)
};
$f.prototype.contains = function(a) {
	a = !this || !a ? j : a instanceof $f ? a.left >= this.left
			&& a.right <= this.right && a.top >= this.top
			&& a.bottom <= this.bottom : a.x >= this.left && a.x <= this.right
			&& a.y >= this.top && a.y <= this.bottom;
	return a
};
function ag(a, b, c, e) {
	this.left = a;
	this.top = b;
	this.width = c;
	this.height = e
}
ag.prototype.va = function() {
	return new ag(this.left, this.top, this.width, this.height)
};
ag.prototype.sk = function(a) {
	var b = Math.max(this.left, a.left), c = Math.min(this.left + this.width,
			a.left + a.width);
	if (b <= c) {
		var e = Math.max(this.top, a.top);
		a = Math.min(this.top + this.height, a.top + a.height);
		if (e <= a) {
			this.left = b;
			this.top = e;
			this.width = c - b;
			this.height = a - e;
			return h
		}
	}
	return j
};
ag.prototype.contains = function(a) {
	return a instanceof ag ? this.left <= a.left
			&& this.left + this.width >= a.left + a.width && this.top <= a.top
			&& this.top + this.height >= a.top + a.height : a.x >= this.left
			&& a.x <= this.left + this.width && a.y >= this.top
			&& a.y <= this.top + this.height
};
function bg(a, b, c) {
	u(b) ? cg(a, c, b) : xb(b, xa(cg, a))
}
function cg(a, b, c) {
	a.style[dg(c)] = b
}
function eg(a, b) {
	var c = Vc(a);
	if (c.defaultView && c.defaultView.getComputedStyle)
		if (c = c.defaultView.getComputedStyle(a, i))
			return c[b] || c.getPropertyValue(b);
	return A
}
function fg(a, b) {
	return eg(a, b) || (a.currentStyle ? a.currentStyle[b] : i) || a.style[b]
}
var gg = "position";
function hg(a) {
	return fg(a, gg)
}
var ig = "1.9";
function jg(a, b, c) {
	var e, f = F && (tc || Ec) && H(ig);
	if (b instanceof lc) {
		e = b.x;
		b = b.y
	} else {
		e = b;
		b = c
	}
	a.style.left = kg(e, f);
	a.style.top = kg(b, f)
}
function lg(a) {
	var b = a.getBoundingClientRect();
	if (E) {
		a = a.ownerDocument;
		b.left -= a.documentElement.clientLeft + a.body.clientLeft;
		b.top -= a.documentElement.clientTop + a.body.clientTop
	}
	return b
}
var mg = "fixed", ng = "absolute", og = "static";
function pg(a) {
	if (E)
		return a.offsetParent;
	var b = Vc(a), c = fg(a, gg), e = c == mg || c == ng;
	for (a = a.parentNode; a && a != b; a = a.parentNode) {
		c = fg(a, gg);
		e = e && c == og && a != b.documentElement && a != b.body;
		if (!e
				&& (a.scrollWidth > a.clientWidth
						|| a.scrollHeight > a.clientHeight || c == mg || c == ng))
			return a
	}
	return i
}
var qg = "overflow", rg = "visible", sg = "borderLeftWidth", tg = "borderRightWidth", ug = "borderTopWidth";
function vg(a) {
	var b = new $f(0, Infinity, Infinity, 0), c = K(a), e = c.D.body, f = !G
			&& ad(c.D) ? c.D.documentElement : c.D.body, g;
	for (a = a; a = pg(a);)
		if ((!E || a.clientWidth != 0)
				&& (!G || a.clientHeight != 0 || a != e)
				&& (a.scrollWidth != a.clientWidth || a.scrollHeight != a.clientHeight)
				&& fg(a, qg) != rg) {
			var k = wg(a), m;
			m = a;
			if (F && !H(ig)) {
				var o = parseFloat(eg(m, sg));
				if (xg(m)) {
					var q = m.offsetWidth - m.clientWidth - o
							- parseFloat(eg(m, tg));
					o += q
				}
				m = new lc(o, parseFloat(eg(m, ug)))
			} else
				m = new lc(m.clientLeft, m.clientTop);
			k.x += m.x;
			k.y += m.y;
			b.top = Math.max(b.top, k.y);
			b.right = Math.min(b.right, k.x + a.clientWidth);
			b.bottom = Math.min(b.bottom, k.y + a.clientHeight);
			b.left = Math.max(b.left, k.x);
			g = g || a != f
		}
	e = f.scrollLeft;
	f = f.scrollTop;
	if (G) {
		b.left += e;
		b.top += f
	} else {
		b.left = Math.max(b.left, e);
		b.top = Math.max(b.top, f)
	}
	if (!g || G) {
		b.right += e;
		b.bottom += f
	}
	c = ld(c.D.parentWindow || c.D.defaultView || window);
	b.right = Math.min(b.right, e + c.width);
	b.bottom = Math.min(b.bottom, f + c.height);
	return b.top >= 0 && b.left >= 0 && b.bottom > b.top && b.right > b.left ? b
			: i
}
var yg = "TR";
function wg(a) {
	var b, c = Vc(a), e = fg(a, gg), f = F && c.getBoxObjectFor
			&& !a.getBoundingClientRect && e == ng
			&& (b = c.getBoxObjectFor(a)) && (b.screenX < 0 || b.screenY < 0), g = new lc(
			0, 0), k;
	b = c ? c.nodeType == 9 ? c : Vc(c) : document;
	if (k = E) {
		k = K(b);
		k = !ad(k.D)
	}
	k = k ? b.body : b.documentElement;
	if (a == k)
		return g;
	if (a.getBoundingClientRect) {
		b = lg(a);
		a = Pd(K(c));
		g.x = b.left + a.x;
		g.y = b.top + a.y
	} else if (c.getBoxObjectFor && !f) {
		b = c.getBoxObjectFor(a);
		a = c.getBoxObjectFor(k);
		g.x = b.screenX - a.screenX;
		g.y = b.screenY - a.screenY
	} else {
		f = a;
		do {
			g.x += f.offsetLeft;
			g.y += f.offsetTop;
			if (f != a) {
				g.x += f.clientLeft || 0;
				g.y += f.clientTop || 0
			}
			if (G && hg(f) == mg) {
				g.x += c.body.scrollLeft;
				g.y += c.body.scrollTop;
				break
			}
			f = f.offsetParent
		} while (f && f != a);
		if (Ac || G && e == ng)
			g.y -= c.body.offsetTop;
		for (f = a; (f = pg(f)) && f != c.body && f != k;) {
			g.x -= f.scrollLeft;
			if (!Ac || f.tagName != yg)
				g.y -= f.scrollTop
		}
	}
	return g
}
function zg(a, b, c) {
	if (b instanceof nc) {
		c = b.height;
		b = b.width
	} else {
		if (c == undefined)
			d(Error("missing height argument"));
		c = c
	}
	a.style.width = kg(b, h);
	a.style.height = kg(c, h)
}
var Ag = "px";
function kg(a, b) {
	if (typeof a == fa)
		a = (b ? Math.round(a) : a) + Ag;
	return a
}
var Bg = "10", Cg = "display", Dg = "none", Eg = "hidden", Fg = "inline";
function Gg(a) {
	var b = Ac && !H(Bg);
	if (fg(a, Cg) != Dg)
		return b ? new nc(a.offsetWidth || a.clientWidth, a.offsetHeight
				|| a.clientHeight) : new nc(a.offsetWidth, a.offsetHeight);
	var c = a.style, e = c.display, f = c.visibility, g = c.position;
	c.visibility = Eg;
	c.position = ng;
	c.display = Fg;
	if (b) {
		b = a.offsetWidth || a.clientWidth;
		a = a.offsetHeight || a.clientHeight
	} else {
		b = a.offsetWidth;
		a = a.offsetHeight
	}
	c.display = e;
	c.position = g;
	c.visibility = f;
	return new nc(b, a)
}
function Hg(a) {
	var b = wg(a);
	a = Gg(a);
	return new ag(b.x, b.y, a.width, a.height)
}
var Ig = {};
function dg(a) {
	return Ig[a] || (Ig[a] = String(a).replace(/\-([a-z])/g, function(b, c) {
		return c.toUpperCase()
	}))
}
var Jg = "opacity", Kg = "MozOpacity", Lg = "filter", Mg = "alpha(opacity=";
function Ng(a, b) {
	var c = a.style;
	if (Jg in c)
		c.opacity = b;
	else if (Kg in c)
		c.MozOpacity = b;
	else if (Lg in c)
		c.filter = b === A ? A : Mg + b * 100 + Zd
}
function U(a, b) {
	a.style.display = b ? A : Dg
}
function Og(a) {
	return a.style.display != Dg
}
var Pg = "rtl", Qg = "direction";
function xg(a) {
	return Pg == fg(a, Qg)
}
var Rg = F ? "MozUserSelect" : G ? "WebkitUserSelect" : i, Sg = "unselectable";
function Tg(a, b, c) {
	c = !c ? a.getElementsByTagName(Zc) : i;
	if (Rg) {
		b = b ? Dg : A;
		a.style[Rg] = b;
		if (c) {
			a = 0;
			for ( var e; e = c[a]; a++)
				e.style[Rg] = b
		}
	} else if (E || Ac) {
		b = b ? gf : A;
		a.setAttribute(Sg, b);
		if (c)
			for (a = 0; e = c[a]; a++)
				e.setAttribute(Sg, b)
	}
};
var Ug = "525";
function Vg(a, b, c, e, f) {
	if (!E && !(G && H(Ug)))
		return h;
	if (tc && f)
		return Wg(a);
	if (f && !e)
		return j;
	if (!c && (b == 17 || b == 18))
		return j;
	if (E && e && b == a)
		return j;
	switch (a) {
	case 13:
		return h;
	case 27:
		return !G
	}
	return Wg(a)
}
function Wg(a) {
	if (a >= 48 && a <= 57)
		return h;
	if (a >= 96 && a <= 106)
		return h;
	if (a >= 65 && a <= 90)
		return h;
	if (G && a == 0)
		return h;
	switch (a) {
	case 32:
	case 63:
	case 107:
	case 109:
	case 110:
	case 111:
	case 186:
	case 189:
	case 187:
	case 188:
	case 190:
	case 191:
	case 192:
	case 222:
	case 219:
	case 220:
	case 221:
		return h;
	default:
		return j
	}
};
function Xg() {
	this.fc = []
}
l = Xg.prototype;
l.Sb = 0;
l.$c = 0;
function Yg(a) {
	if (a.Sb != a.$c) {
		var b = a.fc[a.Sb];
		delete a.fc[a.Sb];
		a.Sb++;
		return b
	}
}
l.Rb = function() {
	return this.$c - this.Sb
};
l.rb = function() {
	return this.$c - this.Sb == 0
};
l.clear = function() {
	this.$c = this.Sb = this.fc.length = 0
};
l.contains = function(a) {
	return kb(this.fc, a)
};
l.remove = function(a) {
	a = eb(this.fc, a);
	if (a < 0)
		return j;
	if (a == this.Sb)
		Yg(this);
	else {
		B.splice.call(this.fc, a, 1);
		this.$c--
	}
	return h
};
l.eb = function() {
	return this.fc.slice(this.Sb, this.$c)
};
function Zg(a, b, c) {
	N.call(this, a, c);
	this.files = b
}
z(Zg, N);
function $g(a, b, c) {
	N.call(this, a, c);
	this.U = b
}
z($g, N);
var ah = "uploadmessagechanged";
function bh(a, b, c) {
	$g.call(this, ah, a, c);
	this.message = b
}
z(bh, $g);
var ch = "filepreviewready";
function dh(a, b, c, e) {
	$g.call(this, ch, a, e);
	this.Uo = b;
	this.Vo = c || i
}
z(dh, $g);
function eh(a, b, c) {
	N.call(this, a, c);
	this.Sq = b
}
z(eh, N);
function fh(a) {
	this.U = a
}
fh.prototype.Yc = i;
fh.prototype.Ae = i;
fh.prototype.ak = i;
fh.prototype.state = nf;
function gh(a) {
	this.U = a
}
z(gh, fh);
gh.prototype.Vj = i;
gh.prototype.Xj = A;
function hh(a) {
	this.U = a
}
z(hh, fh);
hh.prototype.dk = i;
hh.prototype.Nd = i;
hh.prototype.wi = j;
var ih = "GearsFileInfo2(file:", jh = ", persistentId:";
hh.prototype.toString = function() {
	return ih + this.U + jh + this.Nd + Zd
};
function kh(a, b) {
	this.Tb = a || 1;
	this.ff = b || lh;
	this.Pg = w(this.op, this);
	this.Je = ya()
}
z(kh, Q);
kh.prototype.enabled = j;
var lh = n.window;
l = kh.prototype;
l.$ = i;
l.setInterval = function(a) {
	this.Tb = a;
	if (this.$ && this.enabled) {
		this.stop();
		this.start()
	} else
		this.$ && this.stop()
};
var mh = "tick";
l.op = function() {
	if (this.enabled) {
		var a = ya() - this.Je;
		if (a > 0 && a < this.Tb * 0.8)
			this.$ = this.ff.setTimeout(this.Pg, this.Tb - a);
		else {
			this.dispatchEvent(mh);
			if (this.enabled) {
				this.$ = this.ff.setTimeout(this.Pg, this.Tb);
				this.Je = ya()
			}
		}
	}
};
l.start = function() {
	this.enabled = h;
	if (!this.$) {
		this.$ = this.ff.setTimeout(this.Pg, this.Tb);
		this.Je = ya()
	}
};
l.stop = function() {
	this.enabled = j;
	if (this.$) {
		this.ff.clearTimeout(this.$);
		this.$ = i
	}
};
l.h = function() {
	kh.a.h.call(this);
	this.stop();
	delete this.ff
};
function V(a, b, c) {
	if (pa(a)) {
		if (c)
			a = w(a, c)
	} else if (a && typeof a.handleEvent == s)
		a = w(a.handleEvent, a);
	else
		d(Error(Je));
	return b > 2147483647 ? -1 : lh.setTimeout(a, b || 0)
};
var nh = RegExp("^(?:([^:/?#.]+):)?(?://(?:([^/?#]*)@)?([\\w\\d\\-\\u0100-\\uffff.%]*)(?::([0-9]+))?)?([^?#]+)?(?:\\?([^#]*))?(?:#(.*))?$");
var oh = "No Error", ph = "Access denied to content document", qh = "File not found", rh = "Firefox silently errored", sh = "Application custom error", th = "An exception occurred", uh = "Http response at 400 or 500 level", vh = "Request was aborted", wh = "Request timed out", xh = "The resource is not available offline", yh = "Unrecognized error code";
function zh(a) {
	switch (a) {
	case 0:
		return oh;
	case 1:
		return ph;
	case 2:
		return qh;
	case 3:
		return rh;
	case 4:
		return sh;
	case 5:
		return th;
	case 6:
		return uh;
	case 7:
		return vh;
	case 8:
		return wh;
	case 9:
		return xh;
	default:
		return yh
	}
};
function Ah() {
}
Ah.prototype.jj = i;
function Bh(a) {
	var b;
	if (!(b = a.jj)) {
		b = {};
		if (Ch(a)) {
			b[0] = h;
			b[1] = h
		}
		b = a.jj = b
	}
	return b
};
function Dh() {
	return Eh(Fh)
}
var Fh;
function Gh() {
}
z(Gh, Ah);
function Eh(a) {
	return (a = Ch(a)) ? new ActiveXObject(a) : new XMLHttpRequest
}
Gh.prototype.Nh = i;
var Hh = "MSXML2.XMLHTTP.6.0", Ih = "MSXML2.XMLHTTP.3.0", Jh = "MSXML2.XMLHTTP", Kh = "Microsoft.XMLHTTP";
function Ch(a) {
	if (!a.Nh && typeof XMLHttpRequest == "undefined"
			&& typeof ActiveXObject != "undefined") {
		for ( var b = [ Hh, Ih, Jh, Kh ], c = 0; c < b.length; c++) {
			var e = b[c];
			try {
				new ActiveXObject(e);
				return a.Nh = e
			} catch (f) {
			}
		}
		d(Error("Could not create ActiveXObject. ActiveX might be disabled, or MSXML might not be installed"))
	}
	return a.Nh
}
Fh = new Gh;
function Lh(a) {
	this.headers = new D;
	this.Gg = a || i
}
z(Lh, Q);
var Mh = /^https?:?$/i;
l = Lh.prototype;
l.Y = j;
l.T = i;
l.Fg = i;
l.Wa = A;
l.ho = A;
l.ya = 0;
l.Ab = A;
l.dh = j;
l.ag = j;
l.Qh = j;
l.Nc = j;
l.Ag = 0;
l.zc = i;
var Nh = "GET", Oh = "POST", Ph = "Content-Type", Qh = "application/x-www-form-urlencoded;charset=utf-8";
l.send = function(a, b, c, e) {
	if (this.T)
		d(Error("[goog.net.XhrIo] Object is active with another request"));
	b = b || Nh;
	this.Wa = a;
	this.Ab = A;
	this.ya = 0;
	this.ho = b;
	this.dh = j;
	this.Y = h;
	this.T = this.Vg();
	this.Fg = this.Gg ? Bh(this.Gg) : Bh(Fh);
	this.T.onreadystatechange = w(this.Nk, this);
	try {
		this.Qh = h;
		this.T.open(b, a, h);
		this.Qh = j
	} catch (f) {
		Rh(this, f);
		return
	}
	a = c || A;
	var g = this.headers.va();
	e && Mb(e, function(m, o) {
		g.t(o, m)
	});
	b == Oh && !g.Oa(Ph) && g.t(Ph, Qh);
	Mb(g, function(m, o) {
		this.T.setRequestHeader(o, m)
	}, this);
	try {
		if (this.zc) {
			lh.clearTimeout(this.zc);
			this.zc = i
		}
		if (this.Ag > 0)
			this.zc = lh.setTimeout(w(this.pp, this), this.Ag);
		this.ag = h;
		this.T.send(a);
		this.ag = j
	} catch (k) {
		Rh(this, k)
	}
};
l.Vg = function() {
	return this.Gg ? Eh(this.Gg) : new Dh
};
l.dispatchEvent = function(a) {
	return Lh.a.dispatchEvent.call(this, a)
};
var Sh = "Timed out after ", Th = "ms, aborting", Uh = "timeout";
l.pp = function() {
	if (typeof aa != "undefined")
		if (this.T) {
			this.Ab = Sh + this.Ag + Th;
			this.ya = 8;
			this.dispatchEvent(Uh);
			this.abort(8)
		}
};
function Rh(a, b) {
	a.Y = j;
	if (a.T) {
		a.Nc = h;
		a.T.abort();
		a.Nc = j
	}
	a.Ab = b;
	a.ya = 5;
	Vh(a);
	Wh(a)
}
var Xh = "complete";
function Vh(a) {
	if (!a.dh) {
		a.dh = h;
		a.dispatchEvent(Xh);
		a.dispatchEvent(T)
	}
}
l.abort = function(a) {
	if (this.T && this.Y) {
		this.Y = j;
		this.Nc = h;
		this.T.abort();
		this.Nc = j;
		this.ya = a || 7;
		this.dispatchEvent(Xh);
		this.dispatchEvent(Of);
		Wh(this)
	}
};
l.h = function() {
	if (this.T) {
		if (this.Y) {
			this.Y = j;
			this.Nc = h;
			this.T.abort();
			this.Nc = j
		}
		Wh(this, h)
	}
	Lh.a.h.call(this)
};
l.Nk = function() {
	!this.Qh && !this.ag && !this.Nc ? this.Fo() : Yh(this)
};
l.Fo = function() {
	Yh(this)
};
var Zh = "readystatechange", $h = " [";
function Yh(a) {
	if (a.Y)
		if (typeof aa != "undefined")
			if (!(a.Fg[1] && ai(a) == 4 && bi(a) == 2))
				if (a.ag && ai(a) == 4)
					lh.setTimeout(w(a.Nk, a), 0);
				else {
					a.dispatchEvent(Zh);
					if (a.Th()) {
						a.Y = j;
						if (a.vk()) {
							a.dispatchEvent(Xh);
							a.dispatchEvent(S)
						} else {
							a.ya = 6;
							a.Ab = ci(a) + $h + bi(a) + Yd;
							Vh(a)
						}
						Wh(a)
					}
				}
}
var di = "ready";
function Wh(a, b) {
	if (a.T) {
		var c = a.T, e = a.Fg[0] ? p : i;
		a.T = i;
		a.Fg = i;
		if (a.zc) {
			lh.clearTimeout(a.zc);
			a.zc = i
		}
		b || a.dispatchEvent(di);
		try {
			c.onreadystatechange = e
		} catch (f) {
		}
	}
}
l.Pa = function() {
	return !!this.T
};
l.Th = function() {
	return ai(this) == 4
};
l.vk = function() {
	switch (bi(this)) {
	case 0:
		var a;
		a = (a = u(this.Wa) ? this.Wa.match(nh)[1] || i : this.Wa.hb) ? Mh
				.test(a) : self.location ? Mh.test(self.location.protocol) : h;
		return !a;
	case 200:
	case 204:
	case 304:
		return h;
	default:
		return j
	}
};
function ai(a) {
	return a.T ? a.T.readyState : 0
}
function bi(a) {
	try {
		return ai(a) > 2 ? a.T.status : -1
	} catch (b) {
		return -1
	}
}
function ci(a) {
	try {
		return ai(a) > 2 ? a.T.statusText : A
	} catch (b) {
		return A
	}
}
l.Sf = function() {
	return String(this.Wa)
};
l.xb = function() {
	try {
		return this.T ? this.T.responseText : A
	} catch (a) {
		return A
	}
};
l.getResponseHeader = function(a) {
	return this.T && this.Th() ? this.T.getResponseHeader(a) : undefined
};
l.Rf = function() {
	return u(this.Ab) ? this.Ab : String(this.Ab)
};
function ei(a, b) {
	var c;
	if (a instanceof ei) {
		this.Vd(b == i ? a.zb : b);
		fi(this, a.hb);
		gi(this, a.dd);
		hi(this, a.Pb);
		ii(this, a.tc);
		this.Wd(a.fa);
		ji(this, a.Ya.va());
		ki(this, a.Hc)
	} else if (a && (c = String(a).match(nh))) {
		this.Vd(!!b);
		fi(this, c[1] || A, h);
		gi(this, c[2] || A, h);
		hi(this, c[3] || A, h);
		ii(this, c[4]);
		this.Wd(c[5] || A, h);
		ji(this, c[6] || A, h);
		ki(this, c[7] || A, h)
	} else {
		this.Vd(!!b);
		this.Ya = new li(i, this, this.zb)
	}
}
l = ei.prototype;
l.hb = A;
l.dd = A;
l.Pb = A;
l.tc = i;
l.fa = A;
l.Hc = A;
l.$n = j;
l.zb = j;
var mi = "//", ni = "?";
l.toString = function() {
	if (this.jb)
		return this.jb;
	var a = [];
	this.hb && a.push(oi(this.hb, pi), he);
	if (this.Pb) {
		a.push(mi);
		this.dd && a.push(oi(this.dd, pi), Xd);
		var b;
		b = this.Pb;
		b = u(b) ? encodeURIComponent(b) : i;
		a.push(b);
		this.tc != i && a.push(he, String(this.tc))
	}
	if (this.fa) {
		this.Pb && this.fa.charAt(0) != R && a.push(R);
		a.push(oi(this.fa, qi))
	}
	(b = String(this.Ya)) && a.push(ni, b);
	this.Hc && a.push(Xa, oi(this.Hc, ri));
	return this.jb = a.join(A)
};
l.va = function() {
	var a = this.hb, b = this.dd, c = this.Pb, e = this.tc, f = this.fa, g = this.Ya
			.va(), k = this.Hc, m = new ei(i, this.zb);
	a && fi(m, a);
	b && gi(m, b);
	c && hi(m, c);
	e && ii(m, e);
	f && m.Wd(f);
	g && ji(m, g);
	k && ki(m, k);
	return m
};
function fi(a, b, c) {
	si(a);
	delete a.jb;
	a.hb = c ? b ? decodeURIComponent(b) : A : b;
	if (a.hb)
		a.hb = a.hb.replace(/:$/, A);
	return a
}
function gi(a, b, c) {
	si(a);
	delete a.jb;
	a.dd = c ? b ? decodeURIComponent(b) : A : b;
	return a
}
function hi(a, b, c) {
	si(a);
	delete a.jb;
	a.Pb = c ? b ? decodeURIComponent(b) : A : b;
	return a
}
function ii(a, b) {
	si(a);
	delete a.jb;
	if (b) {
		b = Number(b);
		if (isNaN(b) || b < 0)
			d(Error("Bad port number " + b));
		a.tc = b
	} else
		a.tc = i;
	return a
}
l.Wd = function(a, b) {
	si(this);
	delete this.jb;
	this.fa = b ? a ? decodeURIComponent(a) : A : a;
	return this
};
function ji(a, b, c) {
	si(a);
	delete a.jb;
	if (b instanceof li) {
		a.Ya = b;
		a.Ya.cd = a;
		a.Ya.Vd(a.zb)
	} else {
		c || (b = oi(b, ti));
		a.Ya = new li(b, a, a.zb)
	}
	return a
}
function ki(a, b, c) {
	si(a);
	delete a.jb;
	a.Hc = c ? b ? decodeURIComponent(b) : A : b;
	return a
}
function si(a) {
	if (a.$n)
		d(Error("Tried to modify a read-only Uri"))
}
l.Vd = function(a) {
	this.zb = a;
	this.Ya && this.Ya.Vd(a)
};
function ui(a) {
	return a instanceof ei ? a.va() : new ei(a, void 0)
}
var vi = /^[a-zA-Z0-9\-_.!~*'():\/;?]*$/;
function oi(a, b) {
	var c = i;
	if (u(a)) {
		c = a;
		vi.test(c) || (c = encodeURI(a));
		if (c.search(b) >= 0)
			c = c.replace(b, wi)
	}
	return c
}
var xi = "%";
function wi(a) {
	a = a.charCodeAt(0);
	return xi + (a >> 4 & 15).toString(16) + (a & 15).toString(16)
}
var pi = /[#\/\?@]/g, qi = /[\#\?]/g, ti = /[\#\?@]/g, ri = /#/g;
function li(a, b, c) {
	this.Qb = a || i;
	this.cd = b || i;
	this.zb = !!c
}
var yi = "=";
function zi(a) {
	if (!a.R) {
		a.R = new D;
		if (a.Qb)
			for ( var b = a.Qb.split(Fa), c = 0; c < b.length; c++) {
				var e = b[c].indexOf(yi), f = i, g = i;
				if (e >= 0) {
					f = b[c].substring(0, e);
					g = b[c].substring(e + 1)
				} else
					f = b[c];
				f = decodeURIComponent(f.replace(/\+/g, I));
				f = Ai(a, f);
				a.add(f, g ? decodeURIComponent(g.replace(/\+/g, I)) : A)
			}
	}
}
l = li.prototype;
l.R = i;
l.K = i;
l.Rb = function() {
	zi(this);
	return this.K
};
l.add = function(a, b) {
	zi(this);
	Bi(this);
	a = Ai(this, a);
	if (this.Oa(a)) {
		var c = this.R.w(a);
		t(c) ? c.push(b) : this.R.t(a, [ c, b ])
	} else
		this.R.t(a, b);
	this.K++;
	return this
};
l.remove = function(a) {
	zi(this);
	a = Ai(this, a);
	if (this.R.Oa(a)) {
		Bi(this);
		var b = this.R.w(a);
		if (t(b))
			this.K -= b.length;
		else
			this.K--;
		return this.R.remove(a)
	}
	return j
};
l.clear = function() {
	Bi(this);
	this.R && this.R.clear();
	this.K = 0
};
l.rb = function() {
	zi(this);
	return this.K == 0
};
l.Oa = function(a) {
	zi(this);
	a = Ai(this, a);
	return this.R.Oa(a)
};
l.wb = function() {
	zi(this);
	for ( var a = this.R.eb(), b = this.R.wb(), c = [], e = 0; e < b.length; e++) {
		var f = a[e];
		if (t(f))
			for ( var g = 0; g < f.length; g++)
				c.push(b[e]);
		else
			c.push(b[e])
	}
	return c
};
l.eb = function(a) {
	zi(this);
	if (a) {
		a = Ai(this, a);
		if (this.Oa(a)) {
			var b = this.R.w(a);
			if (t(b))
				return b;
			else {
				a = [];
				a.push(b)
			}
		} else
			a = []
	} else {
		b = this.R.eb();
		a = [];
		for ( var c = 0; c < b.length; c++) {
			var e = b[c];
			t(e) ? qb(a, e) : a.push(e)
		}
	}
	return a
};
l.t = function(a, b) {
	zi(this);
	Bi(this);
	a = Ai(this, a);
	if (this.Oa(a)) {
		var c = this.R.w(a);
		if (t(c))
			this.K -= c.length;
		else
			this.K--
	}
	this.R.t(a, b);
	this.K++;
	return this
};
l.w = function(a, b) {
	zi(this);
	a = Ai(this, a);
	if (this.Oa(a)) {
		var c = this.R.w(a);
		return t(c) ? c[0] : c
	} else
		return b
};
l.toString = function() {
	if (this.Qb)
		return this.Qb;
	if (!this.R)
		return A;
	for ( var a = [], b = 0, c = this.R.wb(), e = 0; e < c.length; e++) {
		var f = c[e], g = Ea(f);
		f = this.R.w(f);
		if (t(f))
			for ( var k = 0; k < f.length; k++) {
				b > 0 && a.push(Fa);
				a.push(g);
				f[k] !== A && a.push(yi, Ea(f[k]));
				b++
			}
		else {
			b > 0 && a.push(Fa);
			a.push(g);
			f !== A && a.push(yi, Ea(f));
			b++
		}
	}
	return this.Qb = a.join(A)
};
function Bi(a) {
	delete a.pd;
	delete a.Qb;
	a.cd && delete a.cd.jb
}
l.va = function() {
	var a = new li;
	if (this.pd)
		a.pd = this.pd;
	if (this.Qb)
		a.Qb = this.Qb;
	if (this.R)
		a.R = this.R.va();
	return a
};
function Ai(a, b) {
	var c = String(b);
	if (a.zb)
		c = c.toLowerCase();
	return c
}
l.Vd = function(a) {
	if (a && !this.zb) {
		zi(this);
		Bi(this);
		Mb(this.R, function(b, c) {
			var e = c.toLowerCase();
			if (c != e) {
				this.remove(c);
				this.add(e, b)
			}
		}, this)
	}
	this.zb = a
};
var Ci = "INITIALIZING";
function Di(a, b) {
	if (!a || a.length == 0)
		d(Error("files must be a non-empty array"));
	this.Hm = a;
	this.l = Ci;
	this.Ug = this.Rj = this.Sj = i;
	this.bl = b
}
z(Di, Q);
l = Di.prototype;
l.lf = j;
l.Va = function() {
	return this.Hm
};
l.qa = function(a) {
	this.l = a
};
l.Ad = function() {
	return this.Ug
};
l.rl = function(a) {
	this.lf = a
};
var Ei = "X-GUploader-Client-Info", Fi = "mechanism=", Gi = "; clientVersion=18067216";
l.start = function(a) {
	var b = w(this.Fn, this), c = w(this.Zf, this), e = new Lh;
	O(e, S, b);
	O(e, T, c);
	b = Hi(this);
	try {
		c = {};
		c[Ei] = Fi + this.Ug + Gi;
		var f = be(b);
		e.send(a, Oh, f, c)
	} catch (g) {
		d(g)
	}
};
var Ii = "putInfo", Ji = "formPostInfo", Ki = "startsuccess";
l.Fn = function(a) {
	try {
		var b = ae(a.target.xb());
		if (b.sessionStatus != undefined) {
			var c = b.sessionStatus, e = this.bl ? Ii : Ji;
			this.qa(c.state);
			var f = c.upload_id, g = c.correlation_id, k = c.drop_zone_label, m = this
					.Va();
			for (b = 0; b < c.externalFieldTransfers.length; ++b) {
				var o = c.externalFieldTransfers[b], q = o[e].url;
				if (this.lf)
					q = hi(fi(ui(q), A), A).toString();
				var x = o.content_type, v = m[b];
				v.ie = q;
				v.Cl = f;
				v.hm = g;
				v.fm = x;
				v.Am = k
			}
			this.dispatchEvent(Ki, a.target)
		} else if (b.errorMessage != undefined) {
			var y = b.errorMessage;
			f = y.upload_id;
			m = this.Va();
			C(m, function(Zb) {
				Zb.Cl = f
			});
			Li(this, y);
			this.Zf(a, If)
		} else
			this.Zf(a, Gf)
	} catch (ga) {
		this.Zf(a, Gf)
	}
};
var Mi = "FAILED";
function Ni(a, b) {
	var c = ae(b);
	if (c.sessionStatus != undefined) {
		a.qa(c.sessionStatus.state);
		Li(a, c.sessionStatus)
	} else if (c.errorMessage != undefined) {
		a.qa(Mi);
		Li(a, c.errorMessage)
	}
}
var Oi = "uploader_service.GoogleRupioAdditionalInfo";
function Li(a, b) {
	var c = b.additionalInfo[Oi];
	a.Sj = c.completionInfo.status;
	a.Rj = c.completionInfo.customerSpecificInfo
}
var Pi = "starterror";
l.Zf = function(a, b) {
	this.qa(Mi);
	this.dispatchEvent(Pi, a.target, b)
};
l.dispatchEvent = function(a, b, c) {
	if (u(a))
		a = new Qi(a, this, b, c);
	else {
		a.$k = b;
		a.Mj = c
	}
	try {
		return Di.a.dispatchEvent.call(this, a)
	} catch (e) {
	}
};
var Ri = "0.8";
function Hi(a) {
	var b = {
		protocolVersion : Ri,
		createSessionRequest : {
			fields : []
		}
	};
	C(a.Va(), function(c) {
		Si(this, b, c)
	}, a);
	return b
}
var Ti = "text/plain";
function Si(a, b, c) {
	Ui(a, b.createSessionRequest, c);
	var e = c.Ck;
	wb(e.ed(h), function(f) {
		var g = e.w(f);
		b.createSessionRequest.fields.push({
			inlined : {
				name : f,
				content : g,
				contentType : Ti
			}
		})
	}, a)
}
var Vi = "file", Wi = "put", Xi = "formPost";
function Ui(a, b, c) {
	var e = {
		external : {
			name : Vi,
			filename : c.getName()
		}
	};
	e.external[a.bl ? Wi : Xi] = {};
	a = c.Na;
	if (a != i)
		e.external.size = a;
	b.fields.push(e)
}
var Yi = "Session(state:";
l.toString = function() {
	return Yi + this.l + Zd
};
function Qi(a, b, c, e) {
	N.call(this, a, b);
	this.$k = c || i;
	this.Mj = e || i
}
z(Qi, N);
function Zi() {
	this.gc = [];
	this.wd = new D;
	this.Zc = A;
	this.ad = new Xg;
	this.vf = 0;
	this.mi = 1;
	this.sc = this.Og = h;
	this.ih = i;
	this.lf = this.fj = j
}
z(Zi, Q);
l = Zi.prototype;
l.Ib = h;
l.dispatchEvent = function(a) {
	try {
		return Zi.a.dispatchEvent.call(this, a)
	} catch (b) {
	}
};
l.af = function(a) {
	this.sc = a
};
l.Td = function(a) {
	this.fj = a
};
function $i(a) {
	return a.fj && a.cg()
}
l.cg = function() {
	return j
};
l.Oc = function() {
	return j
};
l.Uh = function() {
	return j
};
l.rl = function(a) {
	this.lf = a
};
l.Da = function(a) {
	return (a = this.wd.w(a)) ? a.U : i
};
function W(a, b) {
	var c = a.wd.w(b.k());
	if (!c) {
		c = a.tf(b);
		a.wd.t(b.k(), c)
	}
	return c
}
l.tf = function(a) {
	return new fh(a)
};
l.$h = function() {
	return j
};
function aj(a) {
	for (; !(a.vf >= a.mi) && !a.ad.rb();)
		bj(a)
}
l.vc = function(a) {
	if (!(this.ad.contains(a) || W(this, a).Yc || W(this, a).state != nf)) {
		if (!(a instanceof cj)) {
			var b = this.ad;
			b.fc[b.$c++] = a
		}
		this.Ga(a, of);
		aj(this)
	}
};
l.Yk = function() {
	C(this.Va(), this.xi, this)
};
l.xi = function(a) {
	this.vc(a);
	if (a instanceof cj) {
		a = a.db;
		for ( var b = 0; b < a.length; b++)
			this.xi(a[b])
	}
};
var dj = "allfilescompleted", ej = "OPEN", fj = "filesessioncreated";
function bj(a) {
	a.ad.rb() && a.dispatchEvent(dj);
	if (!(a.vf >= a.mi || a.ad.rb())) {
		if (/^[\s\xa0]*$/.test(a.Zc))
			d(Error("session server url has not been set"));
		var b = Yg(a.ad);
		if (!(b instanceof cj)) {
			a.vf++;
			var c = new Di([ b ], a.$h());
			c.Ug = a.Ad();
			a.lf && c.rl(h);
			O(c, Ki, a.Pk, j, a);
			O(c, Pi, a.Ho, j, a);
			W(a, b).Yc = c;
			var e = b.ie;
			e && c.qa(ej);
			a.dispatchEvent(new $g(fj, b, a));
			if (e)
				gj(a, b, e);
			else {
				c.start(a.Zc);
				a.Ga(b, pf)
			}
			a.dispatchEvent(new $g(pf, b, a))
		}
	}
}
var hj = "fileiocreated";
function gj(a, b, c) {
	var e = a.nd(b);
	W(a, b).Ae = e;
	a.Ga(b, qf);
	O(e, S, w(a.ti, a));
	O(e, T, w(a.Mo, a));
	O(e, Lf, w(a.si, a));
	a.dispatchEvent(new $g(hj, b, a));
	e.send(c);
	return e
}
l.nd = function(a) {
	return new Ff(a)
};
var ij = "Canceled";
function jj(a, b) {
	switch (W(a, b).state) {
	case S:
	case T:
	case uf:
		return
	}
	var c = W(a, b).Ae, e = W(a, b).Yc;
	e && af(e, Ki, a.Pk, j, a);
	if (c) {
		c.abort();
		W(a, b).Ae = i
	}
	a.ad.remove(b);
	W(a, b).Yc = i;
	a.Ga(b, uf);
	a.dispatchEvent(new bh(b, ij));
	if (b instanceof cj) {
		c = b.db;
		for (e = 0; e < c.length; e++)
			jj(a, c[e])
	}
}
l.Pk = function(a) {
	a = a.target.Va();
	C(a, function(b) {
		var c = b.ie;
		if (c)
			gj(this, b, c);
		else
			d(Error("no upload URL for " + b))
	}, this)
};
l.Ho = function(a) {
	var b = a.target.Va();
	C(b, function(c) {
		this.dispatchEvent(new bh(c, kj(this, a.Mj || a.$k.ya)));
		this.Ga(c, T)
	}, this)
};
var lj = "FINALIZED", mj = "QUEUED", nj = "Server error", oj = "Server returned invalid response";
l.ti = function(a) {
	a = a.target;
	var b = a.Da(), c = W(this, b).Yc;
	try {
		Ni(c, a.xb());
		var e = c.Sj, f = c.l == lj && (e == Pf || e == mj);
		f || this.dispatchEvent(new bh(b, nj));
		this.Ga(b, f ? S : T)
	} catch (g) {
		this.Ga(b, T);
		this.dispatchEvent(new bh(b, oj))
	}
};
var pj = "uploadprogress";
l.si = function(a) {
	a = a.target.Da();
	this.dispatchEvent(new $g(pj, a))
};
l.Mo = function(a) {
	a = a.target;
	var b = a.Da(), c = W(this, b).Yc;
	try {
		Ni(c, a.xb())
	} catch (e) {
	}
	this.dispatchEvent(new bh(b, kj(this, a.ya)));
	this.Ga(b, T)
};
var rj = "httperror", sj = "networkerror", tj = "Lost connection to server", uj = "Connection timed out", vj = "ioerror", wj = "Unable to read file", xj = "securityerror", yj = "Security error", zj = "Server rejected", Aj = "Unexpected error";
function kj(a, b) {
	switch (b) {
	case rj:
	case 6:
		return nj;
	case sj:
	case 9:
		return tj;
	case Uh:
	case 8:
		return uj;
	case vj:
	case 2:
		return wj;
	case xj:
	case 1:
		return yj;
	case If:
		return zj;
	case Gf:
		return oj;
	default:
		return Aj
	}
}
l.Va = function() {
	return ob(this.gc)
};
var Bj = "fileadded";
l.Ec = function(a) {
	this.gc.push(a);
	this.wd.w(a.k()) || this.wd.t(a.k(), this.tf(a));
	this.dispatchEvent(new $g(Bj, a));
	this.nf(a)
};
l.nf = function(a) {
	this.Og && this.vc(a)
};
var Cj = "fileremoved";
l.Wb = function(a) {
	jj(this, a);
	this.wd.remove(a.k());
	mb(this.gc, a);
	this.dispatchEvent(new $g(Cj, a))
};
function Dj(a) {
	C(ob(a.gc), function(b) {
		switch (W(this, b).state) {
		case nf:
		case S:
		case tf:
		case T:
		case uf:
			this.Wb(b)
		}
	}, a)
}
function Ej(a) {
	return kb([ pf, qf, rf, sf ], a)
}
var Fj = "uploadstatechanged";
l.Ga = function(a, b) {
	var c = W(this, a).state;
	if (b != c && Cb(vf, b)) {
		W(this, a).state = b;
		b == uf || b == T || this.dispatchEvent(new bh(a, A));
		this.dispatchEvent(new $g(Fj, a));
		if (Ej(c) && !Ej(b)) {
			this.vf--;
			bj(this)
		}
	}
};
l.Sd = function(a) {
	this.Ib = a
};
function cj(a, b, c, e, f) {
	lf.call(this, a, b, c, e);
	this.ze = f;
	this.db = [];
	this.Kj = {}
}
z(cj, lf);
function Gj(a, b) {
	a.db.push(b);
	a.Kj[b.fa.split(R).pop()] = a.db.length - 1
}
function Hj(a) {
	for ( var b = 0, c = 0, e = a.db, f = 0; f < e.length; f++) {
		var g = e[f];
		if (g instanceof cj) {
			var k = g;
			Hj(k);
			b += k.ze
		} else
			b += 1;
		c += g.Na
	}
	a.Na = c;
	a.ze = b
};
function Ij(a) {
	if (a.length == 0)
		return i;
	var b = a[0].fa;
	b = new cj(b.substring(0, b.indexOf(R)));
	for ( var c = 0; c < a.length; c++) {
		var e = a[c], f = e.fa;
		f = f.substring(0, f.lastIndexOf(R));
		e.Vc = f;
		e.Cf = b;
		f = f.split(R);
		for ( var g = b, k = 1; k < f.length; k++) {
			var m = f[k], o;
			o = g.Kj[m];
			o = o != undefined ? g.db[o] : i;
			if (!o) {
				o = new cj(m, undefined, f.slice(0, k).join(R));
				Gj(g, o)
			}
			g = o
		}
		Gj(g, e)
	}
	Hj(b);
	return b
}
function Jj(a) {
	if (uc)
		return a.replace(/\\/g, R);
	return a
};
var Kj, Lj;
Lj = Kj = j;
var Mj = wc();
if (Mj)
	if (Mj.indexOf("Firefox") == -1)
		if (Mj.indexOf("Camino") == -1)
			if (!(Mj.indexOf("iPhone") != -1 || Mj.indexOf("iPod") != -1))
				if (Mj.indexOf("iPad") == -1)
					if (Mj.indexOf("Android") == -1)
						if (Mj.indexOf("Chrome") != -1)
							Kj = h;
						else if (Mj.indexOf("Safari") != -1)
							Lj = h;
var Nj = Kj, Oj = Lj;
var Pj = "1.5.0_0";
function Qj() {
	if (!navigator.javaEnabled() || Nj && !uc || Oj && uc || Ac)
		return j;
	var a = Rj();
	return !!a && cb(a, Pj) >= 0
}
var Sj = "1.7.0", Tj = "1.6.0", Uj = "1.5.0", Vj = "1.4.2", Wj = "1.7", Xj = "1.6", Yj = "1.5";
function Rj() {
	if (E)
		if (Zj(Sj))
			return Sj;
		else if (Zj(Tj))
			return Tj;
		else if (Zj(Uj))
			return Uj;
		else {
			if (Zj(Vj))
				return Vj
		}
	else if (F || Ac || G || G) {
		var a;
		a: {
			for (a = 0; a < navigator.mimeTypes.length; ++a) {
				var b = navigator.mimeTypes[a].type
						.match(/^application\/x-java-applet;jpi-version=(.*)$/);
				if (b != i) {
					a = b[1];
					break a
				}
			}
			a = i
		}
		if (a != i)
			return a;
		else if ($j(Wj))
			return Sj;
		else if ($j(Xj))
			return Tj;
		else if ($j(Yj))
			return Uj;
		else if ($j(Vj))
			return Vj;
		else if (G)
			if (ak(Sj))
				return Sj;
			else if (ak(Xj))
				return Tj;
			else if (ak(Yj))
				return Uj;
			else if (ak(Vj))
				return Vj
	}
	return i
}
var bk = "JavaWebStart.isInstalled.", ck = ".0";
function Zj(a) {
	if (!ActiveXObject)
		return j;
	try {
		return new ActiveXObject(bk + a + ck) != i
	} catch (b) {
		return j
	}
}
function $j(a) {
	if (!navigator.mimeTypes)
		return j;
	for ( var b = /^application\/x-java-applet\x3Bversion=(1\.8|1\.7|1\.6|1\.5|1\.4\.2)$/, c = 0; c < navigator.mimeTypes.length; ++c) {
		var e = navigator.mimeTypes[c].type.match(b);
		if (e != i)
			if (cb(e[1], a) >= 0)
				return h
	}
	return j
}
function ak(a) {
	if (!navigator.plugins || !navigator.plugins.length)
		return j;
	for ( var b = 0; b < navigator.plugins.length; ++b) {
		var c = navigator.plugins[b].description;
		if (c.search(/^Java Switchable Plug-in (Cocoa)/) != -1) {
			if (cb(Uj, a) >= 0)
				return h
		} else if (c.search(/^Java/) != -1)
			if (uc)
				if (cb(Uj, a) >= 0)
					return h
	}
	if (cb(Uj, a))
		return h;
	return j
};
var dk = "en_US", ek = "Upload", fk = "Select", gk = "1";
function hk(a, b, c, e) {
	Zi.call(this);
	this.Mk = a;
	this.Lg = b;
	this.hg = c || dk;
	this.ra = i;
	this.ub = j;
	this.Zh = this.uq = i;
	this.Y = this.zk = j;
	this.ki = 0;
	this.wm = ek;
	this.ep = fk;
	this.le = gk;
	this.uo = !vc;
	this.qf = e;
	ik(this);
	this.Td(h)
}
z(hk, Zi);
var jk = 1, kk = "JAVA_UPLOADER_", lk = "applet", mk = "com.google.uploader.service.client.applet.UploaderApplet", nk = "position: absolute; top: -", ok = "px; left: -", pk = "px;", qk = "appletloadstart", rk = "appletindom", sk = "appletloadwithpermission", tk = "appletloadnopermission", uk = "appletfailedload";
function ik(a) {
	var b = kk + jk++, c = K(document.body);
	b = c.d(lk, {
		id : b,
		archive : a.Lg,
		code : mk,
		width : a.le,
		height : a.le,
		style : nk + a.le + ok + a.le + pk
	});
	vk(a, new eh(qk, a));
	c.appendChild(document.body, b);
	a.ra = b;
	vk(a, new eh(rk, a));
	a.Zh = window.setInterval(w(function() {
		var e = j;
		try {
			if (this.ra && (this.Y = this.ra.isActive()))
				if (this.zk = this.ra.isLoaded()) {
					vk(this, new eh(sk, this));
					this.Mk(this, h);
					window.clearTimeout(this.Zh)
				} else
					e = h;
			else
				this.ki++
		} catch (f) {
			this.ki++
		}
		if (e || this.ki >= 10) {
			window.clearTimeout(this.Zh);
			this.Y ? vk(this, new eh(tk, this)) : vk(this, new eh(uk, this));
			this.Mk(this, j)
		}
	}, a), 50)
}
l = hk.prototype;
l.$h = function() {
	return h
};
l.nd = function(a) {
	return new Nf(a, this.ra)
};
var wk = "50%", xk = "-";
l.Hb = function(a) {
	this.ub = a;
	if (this.zk && this.Ib) {
		this.Ib = j;
		bg(this.ra, {
			top : wk,
			left : wk
		});
		window.setTimeout(w(function() {
			this.ra.setLocale(this.hg);
			var b = this.ra.selectFiles(this.wm, this.ep, this.sc, this.ih,
					$i(this), this.uo), c = xk + this.le + Ag;
			bg(this.ra, {
				top : c,
				left : c
			});
			this.Md(b);
			this.Ib = h
		}, this), 50)
	}
};
l.Wb = function(a) {
	hk.a.Wb.call(this, a);
	this.ra.freeResources(a.xc)
};
var yk = "scotty java";
l.Ad = function() {
	return yk
};
l.cg = function() {
	return h
};
function vk(a, b) {
	a.qf && a.qf(b)
}
l.Yk = function() {
	C(this.Va(), this.xi, this)
};
var zk = "filesselected";
l.Md = function(a) {
	this.ub && Dj(this);
	a = $d(a);
	a = gb(a, w(function(b) {
		var c = Ak(this, b);
		c instanceof cj && Bk(this, b.c, c, c);
		return c
	}, this));
	this.dispatchEvent(new Zg(zk, a));
	C(a, function(b) {
		this.Ec(b)
	}, this)
};
function Bk(a, b, c, e) {
	for ( var f = 0; f < b.length; f++) {
		var g = b[f], k = Ak(a, g);
		k.Cf = e;
		k instanceof cj && Bk(a, g.c, k, e);
		Gj(c, k)
	}
}
function Ak(a, b) {
	var c = b.r;
	if (c != undefined)
		c = Jj(c);
	return b.f ? new cj(b.n, b.s, c, b.i, b.f) : new lf(b.n, b.s, c, b.i)
};
function Ck(a) {
	this.da = a
}
z(Ck, Qd);
var Dk = new De(0, 100);
l = Ck.prototype;
l.e = function(a, b, c, e, f) {
	if (t(b))
		for ( var g = 0; g < b.length; g++)
			this.e(a, b[g], c, e, f);
	else {
		a = O(a, b, c || this, e || j, f || this.da || this);
		if (this.H)
			this.H[a] = h;
		else if (this.qc) {
			this.H = Ee(Dk);
			this.H[this.qc] = h;
			this.qc = i;
			this.H[a] = h
		} else
			this.qc = a
	}
	return this
};
l.Aa = function(a, b, c, e, f) {
	if (this.qc || this.H)
		if (t(b))
			for ( var g = 0; g < b.length; g++)
				this.Aa(a, b[g], c, e, f);
		else {
			a: {
				c = c || this;
				f = f || this.da || this;
				e = !!e;
				if (a = bf(a, b, e))
					for (b = 0; b < a.length; b++)
						if (a[b].Qc == c && a[b].capture == e && a[b].$f == f) {
							a = a[b];
							break a
						}
				a = i
			}
			if (a) {
				a = a.key;
				cf(a);
				if (this.H)
					Eb(this.H, a);
				else if (this.qc == a)
					this.qc = i
			}
		}
	return this
};
l.Pd = function() {
	if (this.H) {
		for ( var a in this.H) {
			cf(a);
			delete this.H[a]
		}
		Fe(Dk, this.H);
		this.H = i
	} else
		this.qc && cf(this.qc)
};
l.h = function() {
	Ck.a.h.call(this);
	this.Pd()
};
l.handleEvent = function() {
	d(Error("EventHandler.handleEvent not implemented"))
};
var Ek = "role";
function Fk(a, b) {
	a.setAttribute(Ek, b);
	a.Lq = b
}
var Gk = "aria-";
function X(a, b, c) {
	a.setAttribute(Gk + b, c)
};
var Hk = "focusout", Ik = "blur", Jk = "focusin", Kk = "focus";
function Lk(a) {
	this.m = a;
	a = E ? Hk : Ik;
	this.ko = O(this.m, E ? Jk : Kk, this, !E);
	this.lo = O(this.m, a, this, !E)
}
z(Lk, Q);
Lk.prototype.handleEvent = function(a) {
	var b = new we(a.Ja);
	b.type = a.type == Jk || a.type == Kk ? Jk : Hk;
	try {
		this.dispatchEvent(b)
	} finally {
		b.u()
	}
};
Lk.prototype.h = function() {
	Lk.a.h.call(this);
	cf(this.ko);
	cf(this.lo);
	delete this.m
};
var Mk = "mousedown";
function Nk(a, b, c) {
	this.target = a;
	this.handle = b || a;
	this.ji = c || new ag(NaN, NaN, NaN, NaN);
	this.D = Vc(a);
	this.N = new Ck(this);
	O(this.handle, Mk, this.tl, j, this)
}
z(Nk, Q);
var Ok = E || F && H("1.9.3");
l = Nk.prototype;
l.screenX = 0;
l.screenY = 0;
l.ul = 0;
l.vl = 0;
l.se = 0;
l.te = 0;
l.lb = h;
l.ec = j;
l.qk = 0;
l.qo = 0;
l.On = j;
l.ja = function() {
	return this.N
};
l.Cb = function(a) {
	this.lb = a
};
l.h = function() {
	Nk.a.h.call(this);
	af(this.handle, Mk, this.tl, j, this);
	this.N.u();
	delete this.target;
	delete this.handle;
	delete this.N
};
var Pk = "mousemove", Qk = "mouseup", Rk = "losecapture", Sk = "dragstart", Tk = "scroll";
l.tl = function(a) {
	if (this.lb && !this.ec && (a.type != Mk || Ce(a, 0))) {
		if (this.qk == 0) {
			Uk(this, a);
			if (this.ec)
				a.preventDefault();
			else
				return
		} else
			a.preventDefault();
		var b = this.D, c = b.documentElement, e = !Ok;
		this.N.e(b, Pk, this.ro, e);
		this.N.e(b, Qk, this.Ff, e);
		if (Ok) {
			c.setCapture(j);
			this.N.e(c, Rk, this.Ff)
		} else
			this.N.e(md(b), Ik, this.Ff);
		E && this.On && this.N.e(b, Sk, ve);
		this.cp && this.N.e(this.cp, Tk, this.Go, e);
		this.screenX = this.ul = a.screenX;
		this.screenY = this.vl = a.screenY;
		this.se = this.target.offsetLeft;
		this.te = this.target.offsetTop;
		this.Re = Pd(K(this.D));
		this.qo = ya()
	}
};
function Uk(a, b) {
	if (a.dispatchEvent(new Vk(pf, a, b.clientX, b.clientY, b)) !== j)
		a.ec = h
}
var Wk = "end";
l.Ff = function(a, b) {
	this.N.Pd();
	Ok && this.D.releaseCapture();
	if (this.ec) {
		this.ec = j;
		this.dispatchEvent(new Vk(Wk, this, a.clientX, a.clientY, a, Xk(this,
				this.se), Yk(this, this.te), b))
	}
};
var Zk = "beforedrag";
l.ro = function(a) {
	if (this.lb) {
		var b = a.screenX - this.screenX, c = a.screenY - this.screenY;
		this.screenX = a.screenX;
		this.screenY = a.screenY;
		if (!this.ec) {
			var e = this.ul - this.screenX, f = this.vl - this.screenY;
			if (e * e + f * f > this.qk) {
				Uk(this, a);
				if (!this.ec) {
					this.Ff(a);
					return
				}
			}
		}
		c = $k(this, b, c);
		b = c.x;
		c = c.y;
		if (this.ec)
			if (this.dispatchEvent(new Vk(Zk, this, a.clientX, a.clientY, a, b,
					c)) !== j) {
				al(this, a, b, c, j);
				a.preventDefault()
			}
	}
};
function $k(a, b, c) {
	var e = Pd(K(a.D));
	b += e.x - a.Re.x;
	c += e.y - a.Re.y;
	a.Re = e;
	a.se += b;
	a.te += c;
	return new lc(Xk(a, a.se), Yk(a, a.te))
}
l.Go = function(a) {
	var b = $k(this, 0, 0);
	a.clientX = this.Re.x - this.screenX;
	a.clientY = this.Re.x - this.screenY;
	al(this, a, b.x, b.y, h)
};
var bl = "drag";
function al(a, b, c, e) {
	a.target.style.left = c + Ag;
	a.target.style.top = e + Ag;
	a.dispatchEvent(new Vk(bl, a, b.clientX, b.clientY, b, c, e))
}
function Xk(a, b) {
	var c = a.ji, e = !isNaN(c.left) ? c.left : i;
	c = !isNaN(c.width) ? c.width : 0;
	return Math.min(e != i ? e + c : Infinity, Math.max(e != i ? e : -Infinity,
			b))
}
function Yk(a, b) {
	var c = a.ji, e = !isNaN(c.top) ? c.top : i;
	c = !isNaN(c.height) ? c.height : 0;
	return Math.min(e != i ? e + c : Infinity, Math.max(e != i ? e : -Infinity,
			b))
}
function Vk(a, b, c, e, f, g, k, m) {
	N.call(this, a);
	this.clientX = c;
	this.clientY = e;
	this.Qp = f;
	this.left = g !== undefined ? g : b.se;
	this.top = k !== undefined ? k : b.te;
	this.Zp = b;
	this.Yp = !!m
}
z(Vk, N);
function cl() {
}
r(cl);
cl.prototype.wo = 0;
cl.O();
function Y(a) {
	this.Ob = a || K();
	this.We = dl
}
z(Y, Q);
Y.prototype.Nn = cl.O();
var dl = i, el = "disable", fl = "enable", gl = "highlight", hl = "unhighlight", il = "activate", jl = "deactivate", kl = "select", ll = "unselect", ml = "check", nl = "uncheck", ol = "open", pl = "close";
function ql(a, b) {
	switch (a) {
	case 1:
		return b ? el : fl;
	case 2:
		return b ? gl : hl;
	case 4:
		return b ? il : jl;
	case 8:
		return b ? kl : ll;
	case 16:
		return b ? ml : nl;
	case 32:
		return b ? Kk : Ik;
	case 64:
		return b ? ol : pl
	}
	d(Error("Invalid component state"))
}
l = Y.prototype;
l.na = i;
l.C = j;
l.m = i;
l.We = i;
l.Rc = i;
l.pa = i;
l.Ia = i;
l.kb = i;
l.Jl = j;
l.k = function() {
	return this.na || (this.na = he + (this.Nn.wo++).toString(36))
};
function rl(a, b) {
	if (a.pa && a.pa.kb) {
		Eb(a.pa.kb, a.na);
		Fb(a.pa.kb, b, a)
	}
	a.na = b
}
l.b = function() {
	return this.m
};
l.ja = function() {
	return this.Hd || (this.Hd = new Ck(this))
};
var sl = "Unable to set parent component";
function tl(a, b) {
	if (a == b)
		d(Error(sl));
	if (b && a.pa && a.na && ul(a.pa, a.na) && a.pa != b)
		d(Error(sl));
	a.pa = b;
	Y.a.Ii.call(a, b)
}
l.X = function() {
	return this.pa
};
l.Ii = function(a) {
	if (this.pa && this.pa != a)
		d(Error("Method not supported"));
	Y.a.Ii.call(this, a)
};
l.g = function() {
	return this.Ob
};
l.d = function() {
	this.m = this.Ob.createElement(L)
};
l.Za = function(a) {
	vl(this, a)
};
var wl = "Component already rendered";
function vl(a, b, c) {
	if (a.C)
		d(Error(wl));
	a.m || a.d();
	b ? b.insertBefore(a.m, c || i) : a.Ob.D.body.appendChild(a.m);
	if (!a.pa || a.pa.C)
		a.B()
}
l.G = function(a) {
	if (this.C)
		d(Error(wl));
	else if (a && this.Ha(a)) {
		this.Jl = h;
		if (!this.Ob || this.Ob.D != Vc(a))
			this.Ob = K(a);
		this.Ua(a);
		this.B()
	} else
		d(Error("Invalid element to decorate"))
};
l.Ha = function() {
	return h
};
l.Ua = function(a) {
	this.m = a
};
l.B = function() {
	this.C = h;
	xl(this, function(a) {
		!a.C && a.b() && a.B()
	})
};
l.P = function() {
	xl(this, function(a) {
		a.C && a.P()
	});
	this.Hd && this.Hd.Pd();
	this.C = j
};
l.h = function() {
	Y.a.h.call(this);
	this.C && this.P();
	if (this.Hd) {
		this.Hd.u();
		delete this.Hd
	}
	xl(this, function(a) {
		a.u()
	});
	!this.Jl && this.m && yd(this.m);
	this.pa = this.Rc = this.m = this.kb = this.Ia = i
};
l.Dc = function(a, b) {
	this.$b(a, yl(this), b)
};
l.$b = function(a, b, c) {
	if (a.C && (c || !this.C))
		d(Error(wl));
	if (b < 0 || b > yl(this))
		d(Error("Child component index out of bounds"));
	if (!this.kb || !this.Ia) {
		this.kb = {};
		this.Ia = []
	}
	if (a.X() == this) {
		this.kb[a.k()] = a;
		mb(this.Ia, a)
	} else
		Fb(this.kb, a.k(), a);
	tl(a, this);
	rb(this.Ia, b, 0, a);
	if (a.C && this.C && a.X() == this) {
		c = this.Q();
		c.insertBefore(a.b(), c.childNodes[b] || i)
	} else if (c) {
		this.m || this.d();
		b = Z(this, b + 1);
		vl(a, this.Q(), b ? b.m : i)
	} else
		this.C && !a.C && a.m && a.B()
};
l.Q = function() {
	return this.m
};
function zl(a) {
	if (a.We == i)
		a.We = xg(a.C ? a.m : a.Ob.D.body);
	return a.We
}
l.Xd = function(a) {
	if (this.C)
		d(Error(wl));
	this.We = a
};
function Al(a) {
	return !!a.Ia && a.Ia.length != 0
}
function yl(a) {
	return a.Ia ? a.Ia.length : 0
}
function ul(a, b) {
	var c;
	if (a.kb && b) {
		c = a.kb;
		c = b in c ? c[b] : void 0;
		c = c || i
	} else
		c = i;
	return c
}
function Z(a, b) {
	return a.Ia ? a.Ia[b] || i : i
}
function xl(a, b, c) {
	a.Ia && C(a.Ia, b, c)
}
function Bl(a, b) {
	return a.Ia && b ? eb(a.Ia, b) : -1
}
l.removeChild = function(a, b) {
	if (a) {
		var c = u(a) ? a : a.k();
		a = ul(this, c);
		if (c && a) {
			Eb(this.kb, c);
			mb(this.Ia, a);
			if (b) {
				a.P();
				a.m && yd(a.m)
			}
			tl(a, i)
		}
	}
	if (!a)
		d(Error("Child is not in parent component"));
	return a
};
var Cl = "modal-dialog";
function Dl(a, b, c) {
	Y.call(this, c);
	this.ua = a || Cl;
	this.Wi = !!b;
	this.sa = El()
}
z(Dl, Y);
l = Dl.prototype;
l.ic = i;
l.Dm = h;
l.Lh = h;
l.Wi = j;
l.kg = h;
l.Ij = h;
l.bm = 0.5;
l.Bg = A;
l.Ca = A;
l.sa = i;
l.ud = i;
l.aa = j;
l.xm = j;
l.la = i;
l.ha = i;
l.ib = i;
l.Fb = i;
l.Qi = i;
l.Zb = i;
l.cb = i;
l.ca = i;
l.$a = function(a) {
	this.Ca = a;
	if (this.cb)
		this.cb.innerHTML = a
};
l.Q = function() {
	this.b() || this.Za();
	return this.cb
};
var Fl = "-title", Gl = "span", Hl = "-title-text", Il = "-title-close", Jl = "-content", Kl = "-buttons", Ll = "dialog", Ml = "labelledby";
l.d = function() {
	Nl(this);
	var a = this.g();
	this.m = a.d(L, {
		className : this.ua,
		tabIndex : 0
	}, this.ib = a.d(L, {
		className : this.ua + Fl,
		id : this.k()
	}, this.Fb = a.d(Gl, this.ua + Hl, this.Bg), this.Zb = a
			.d(Gl, this.ua + Il)), this.cb = a.d(L, this.ua + Jl), this.ca = a
			.d(L, this.ua + Kl), this.yl = a.d(Gl, {
		tabIndex : 0
	}));
	this.Qi = this.ib.id;
	Fk(this.b(), Ll);
	X(this.b(), Ml, this.Qi || A);
	if (this.Ca)
		this.cb.innerHTML = this.Ca;
	U(this.Zb, this.Lh);
	U(this.b(), j);
	if (this.sa) {
		a = this.sa;
		a.m = this.ca;
		a.Za()
	}
};
var Ol = "iframe", Pl = "border:0;vertical-align:bottom;", Ql = 'javascript:""', Rl = "-bg";
function Nl(a) {
	if (a.Wi && a.kg && !a.ha) {
		var b;
		b = a.g().d(Ol, {
			frameborder : 0,
			style : Pl,
			src : Ql
		});
		a.ha = b;
		a.ha.className = a.ua + Rl;
		U(a.ha, j);
		Ng(a.ha, 0)
	} else if ((!a.Wi || !a.kg) && a.ha) {
		yd(a.ha);
		a.ha = i
	}
	if (a.kg && !a.la) {
		a.la = a.g().d(L, a.ua + Rl);
		Ng(a.la, a.bm);
		U(a.la, j)
	} else if (!a.kg && a.la) {
		yd(a.la);
		a.la = i
	}
}
l.Za = function(a) {
	if (this.C)
		d(Error(wl));
	this.b() || this.d();
	a = a || Nd(this.g()).body;
	Sl(this, a);
	Dl.a.Za.call(this, a)
};
function Sl(a, b) {
	a.ha && b.appendChild(a.ha);
	a.la && b.appendChild(a.la)
}
var Tl = "DIV";
l.Ha = function(a) {
	return a && a.tagName && a.tagName == Tl && Dl.a.Ha.call(this, a)
};
l.Ua = function(a) {
	Dl.a.Ua.call(this, a);
	J(this.b(), this.ua);
	a = this.ua + Jl;
	if (this.cb = Xc(i, a, this.b())[0])
		this.Ca = this.cb.innerHTML;
	else {
		this.cb = this.g().d(L, a);
		if (this.Ca)
			this.cb.innerHTML = this.Ca;
		this.b().appendChild(this.cb)
	}
	a = this.ua + Fl;
	var b = this.ua + Hl, c = this.ua + Il;
	if (this.ib = Xc(i, a, this.b())[0]) {
		this.Fb = Xc(i, b, this.ib)[0];
		this.Zb = Xc(i, c, this.ib)[0]
	} else {
		this.ib = this.g().d(L, a);
		this.b().insertBefore(this.ib, this.cb)
	}
	if (this.Fb)
		this.Bg = Ld(this.Fb);
	else {
		this.Fb = this.g().d(Gl, b, this.Bg);
		this.ib.appendChild(this.Fb)
	}
	X(this.b(), Ml, this.Qi || A);
	if (!this.Zb) {
		this.Zb = this.g().d(Gl, c);
		this.ib.appendChild(this.Zb)
	}
	U(this.Zb, this.Lh);
	a = this.ua + Kl;
	if (this.ca = Xc(i, a, this.b())[0]) {
		this.sa = new Ul(this.g());
		this.sa.G(this.ca)
	} else {
		this.ca = this.g().d(L, a);
		this.b().appendChild(this.ca);
		if (this.sa) {
			a = this.sa;
			a.m = this.ca;
			a.Za()
		}
	}
	Nl(this);
	Sl(this, Vc(this.b()).body);
	U(this.b(), j)
};
var Vl = "-title-draggable";
l.B = function() {
	Dl.a.B.call(this);
	this.ic = new Lk(Nd(this.g()));
	if (this.Ij && !this.ud) {
		var a = new Nk(this.b(), this.ib);
		J(this.ib, this.ua + Vl);
		this.ud = a
	}
	this.ja().e(this.Zb, Be, this.Lo).e(this.ic, Jk, this.Do);
	Fk(this.b(), Ll);
	this.Fb.id !== A && X(this.b(), Ml, this.Fb.id)
};
l.P = function() {
	this.V() && this.A(j);
	this.ic.u();
	this.ic = i;
	if (this.ud) {
		this.ud.u();
		this.ud = i
	}
	Dl.a.P.call(this)
};
var Wl = "keydown", Xl = "resize", Yl = "afterhide";
l.A = function(a) {
	if (a != this.aa) {
		var b = Nd(this.g()), c = md(b) || window;
		this.C || this.Za(b.body);
		if (a) {
			Zl(this);
			this.Ra();
			this.ja().e(this.b(), Wl, this.og).e(this.b(), Ae, this.og).e(c,
					Xl, this.Ok)
		} else
			this.ja().Aa(this.b(), Wl, this.og).Aa(this.b(), Ae, this.og).Aa(c,
					Xl, this.Ok);
		this.ha && U(this.ha, a);
		this.la && U(this.la, a);
		U(this.b(), a);
		a && this.focus();
		if (this.aa = a)
			this.ja().e(this.ca, Be, this.Ik);
		else {
			this.ja().Aa(this.ca, Be, this.Ik);
			this.dispatchEvent(Yl);
			this.xm && this.u()
		}
	}
};
l.V = function() {
	return this.aa
};
var $l = "button", am = "input", bm = "position:fixed;width:0;height:0;left:0;top:0;";
l.focus = function() {
	try {
		this.b().focus()
	} catch (a) {
	}
	if (this.sa) {
		var b = this.sa.yf;
		if (b)
			for ( var c = Nd(this.g()), e = this.ca.getElementsByTagName($l), f = 0, g; g = e[f]; f++)
				if (g.name == b) {
					try {
						if (G || Ac) {
							var k = c.createElement(am);
							k.style.cssText = bm;
							this.b().appendChild(k);
							k.focus();
							this.b().removeChild(k)
						}
						g.focus()
					} catch (m) {
					}
					break
				}
	}
};
function Zl(a) {
	a.ha && U(a.ha, j);
	a.la && U(a.la, j);
	var b = Nd(a.g()), c = ld(md(b) || window || window), e = Math.max(
			b.body.scrollWidth, c.width);
	b = Math.max(b.body.scrollHeight, c.height);
	if (a.ha) {
		U(a.ha, h);
		zg(a.ha, e, b)
	}
	if (a.la) {
		U(a.la, h);
		zg(a.la, e, b)
	}
	if (a.Ij) {
		c = Gg(a.b());
		a.ud.ji = new ag(0, 0, e - c.width, b - c.height)
	}
}
l.Ra = function() {
	var a = Nd(this.g()), b = md(a) || window;
	if (hg(this.b()) == mg)
		var c = a = 0;
	else {
		c = Pd(this.g());
		a = c.x;
		c = c.y
	}
	var e = Gg(this.b());
	b = ld(b || window);
	a = Math.max(a + b.width / 2 - e.width / 2, 0);
	c = Math.max(c + b.height / 2 - e.height / 2, 0);
	jg(this.b(), a, c)
};
l.Lo = function() {
	if (this.Lh) {
		var a = this.sa, b = a && a.Sg;
		if (b) {
			a = a.w(b);
			this.dispatchEvent(new cm(b, a)) && this.A(j)
		} else
			this.A(j)
	}
};
l.h = function() {
	Dl.a.h.call(this);
	if (this.la) {
		yd(this.la);
		this.la = i
	}
	if (this.ha) {
		yd(this.ha);
		this.ha = i
	}
	this.yl = this.ca = this.Zb = i
};
l.Gi = function(a) {
	this.sa = a;
	if (this.ca)
		if (this.sa) {
			a = this.sa;
			a.m = this.ca;
			a.Za()
		} else
			this.ca.innerHTML = A
};
var dm = "BUTTON";
l.Ik = function(a) {
	a: {
		for (a = a.target; a != i && a != this.ca;) {
			if (a.tagName == dm) {
				a = a;
				break a
			}
			a = a.parentNode
		}
		a = i
	}
	if (a && !a.disabled) {
		a = a.name;
		var b = this.sa.w(a);
		this.dispatchEvent(new cm(a, b)) && this.A(j)
	}
};
var em = "SELECT", fm = "TEXTAREA";
l.og = function(a) {
	var b = j, c = j, e = this.sa, f = a.target;
	if (a.type == Wl)
		if (this.Dm && a.keyCode == 27) {
			var g = e && e.Sg;
			f = f.tagName == em && !f.disabled;
			if (g && !f) {
				c = h;
				b = e.w(g);
				b = this.dispatchEvent(new cm(g, b))
			} else
				f || (b = h)
		} else {
			if (a.keyCode == 9 && a.shiftKey && f == this.b())
				c = h
		}
	else if (a.keyCode == 13) {
		if (f.tagName == dm)
			g = f.name;
		else if (e) {
			var k = e.yf, m;
			if (m = k)
				a: {
					m = e.m.getElementsByTagName(dm);
					for ( var o = 0, q; q = m[o]; o++)
						if (q.name == k || q.id == k) {
							m = q;
							break a
						}
					m = i
				}
			m = m;
			f = (f.tagName == fm || f.tagName == em) && !f.disabled;
			if (m && !m.disabled && !f)
				g = k
		}
		if (g) {
			c = h;
			b = this.dispatchEvent(new cm(g, String(e.w(g))))
		}
	}
	if (b || c) {
		a.stopPropagation();
		a.preventDefault()
	}
	b && this.A(j)
};
l.Ok = function() {
	Zl(this)
};
l.Do = function(a) {
	this.yl == a.target && V(this.Lm, 0, this)
};
l.Lm = function() {
	E && Nd(this.g()).body.focus();
	this.b().focus()
};
var gm = "dialogselect";
function cm(a, b) {
	this.type = gm;
	this.key = a;
	this.caption = b
}
z(cm, N);
function Ul(a) {
	this.Ob = a || K();
	D.call(this)
}
var hm;
z(Ul, D);
l = Ul.prototype;
l.ua = "goog-buttonset";
l.yf = i;
l.m = i;
l.Sg = i;
l.t = function(a, b, c, e) {
	D.prototype.t.call(this, a, b);
	if (c)
		this.yf = a;
	if (e)
		this.Sg = a;
	return this
};
function im(a, b, c, e) {
	return a.t(b.key, b.caption, c, e)
}
var jm = "-default";
l.Za = function() {
	if (this.m) {
		this.m.innerHTML = A;
		var a = K(this.m);
		Mb(this, function(b, c) {
			var e = a.d($l, {
				name : c
			}, b);
			if (c == this.yf)
				e.className = this.ua + jm;
			this.m.appendChild(e)
		}, this)
	}
};
l.G = function(a) {
	if (!(!a || a.nodeType != 1)) {
		this.m = a;
		a = this.m.getElementsByTagName($l);
		for ( var b = 0, c, e, f; c = a[b]; b++) {
			e = c.name || c.id;
			f = Ld(c) || c.value;
			if (e) {
				var g = b == 0;
				this.t(e, f, g, c.name == uf);
				g && J(c, this.ua + jm)
			}
		}
	}
};
var km = "ok", lm = {
	key : km,
	caption : "OK"
}, mm = "Cancel", nm = {
	key : uf,
	caption : mm
}, om = {
	key : "yes",
	caption : "Yes"
}, pm = "no", qm = {
	key : pm,
	caption : "No"
};
function El() {
	return im(im(new Ul, lm, h), nm, j, h)
}
im(new Ul, lm, h, h);
hm = El();
im(im(new Ul, om, h), qm, j, h);
im(im(im(new Ul, om), qm, h), nm, j, h);
im(im(im(new Ul, {
	key : "continue",
	caption : "Continue"
}), {
	key : "save",
	caption : "Save"
}), nm, h, h);
var rm = "top", sm = "left", tm = "target", um = "noreferrer", vm = '<META HTTP-EQUIV="refresh" content="0; url=', wm = '">';
function xm(a) {
	var b;
	b || (b = {});
	var c = window, e = typeof a.href != "undefined" ? a.href : String(a);
	a = b.target || a.target;
	var f = [], g;
	for (g in b)
		switch (g) {
		case hd:
		case gd:
		case rm:
		case sm:
			f.push(g + yi + b[g]);
			break;
		case tm:
		case um:
			break;
		default:
			f.push(g + yi + (b[g] ? 1 : 0))
		}
	g = f.join(fe);
	if (b.noreferrer) {
		if (b = c.open(A, a, g)) {
			b.document.write(vm + Na(e) + wm);
			b.document.close()
		}
	} else
		b = c.open(e, a, g);
	return b
};
var ym = "Uploading folders", zm = "chrome", Am = "Download Chrome", Bm = "Install applet";
function Cm(a, b) {
	Dl.call(this);
	this.j = a;
	this.co = b;
	this.N = new Ck(this);
	this.N.e(this, gm, this.an);
	this.Bg = ym;
	this.Fb && Ed(this.Fb, ym);
	var c = new Ul;
	this.j.Yg && c.t(zm, Am);
	Qj() && this.j.ve && c.t(lk, Bm);
	c.t(uf, mm, j, h);
	this.Gi(c)
}
z(Cm, Dl);
var Dm = "<p>", Em = "Your browser doesn't support the uploading of entire folders.", Fm = "</p><p>", Gm = "If you would like to use this feature, install the Chrome browser (recommended) or the Folder Upload applet.", Hm = "If you would like to use this feature, install the Chrome web browser.", Im = "If you would like to use this feature, install the Folder Upload applet.", Jm = "No methods to upload entire folders are currently available.", Km = "</p>", Lm = "Note: Your browser does not have Java enabled. You must enable Java before using the Folder Upload applet.", Mm = '<p><a href="', Nm = '" target="_blank">', Om = "Learn more", Pm = "</a></p>";
Cm.prototype.d = function() {
	Cm.a.d.call(this);
	var a = {
		Xn : Qj(),
		jo : this.j.Zj,
		Xh : this.j.ve,
		tk : this.j.Yg
	}, b = this.Q();
	var c = new M;
	c.append(Dm, Em, Fm);
	if (a.tk && a.Xh)
		c.append(Gm);
	else if (a.tk)
		c.append(Hm);
	else
		a.Xh ? c.append(Im) : c.append(Jm);
	c.append(Km);
	a.Xh && !a.Xn && c.append(Dm, Lm, Km);
	c.append(Mm, Wd(a.jo), Nm, Om, Pm);
	a = c.toString();
	b.innerHTML = a
};
var Qm = "info";
Cm.prototype.an = function(a) {
	var b = this.j;
	if (a.key == Qm)
		xm(b.Zj);
	else if (a.key == lk)
		this.co();
	else
		a.key == zm && xm(b.em)
};
Cm.prototype.h = function() {
	Cm.a.h.call(this);
	this.N.u();
	delete this.N;
	delete this.j
};
function Rm() {
}
var Sm;
r(Rm);
l = Rm.prototype;
l.nb = function() {
};
l.d = function(a) {
	return a.g().d(L, this.kc(a).join(I), a.Ca)
};
l.Q = function(a) {
	return a
};
var Tm = "7";
l.vd = function(a, b, c) {
	if (a = a.b ? a.b() : a)
		if (E && !H(Tm)) {
			var e = Um(this, Sc(a), b);
			e.push(b);
			xa(c ? J : Tc, a).apply(i, e)
		} else
			c ? J(a, b) : Tc(a, b)
};
l.Ha = function() {
	return h
};
l.G = function(a, b) {
	b.id && rl(a, b.id);
	var c = this.Q(b);
	if (c && c.firstChild)
		Vm(a, c.firstChild.nextSibling ? ob(c.childNodes) : c.firstChild);
	else
		a.Ca = i;
	var e = 0, f = this.o(), g = this.o(), k = j, m = j;
	c = j;
	var o = Sc(b);
	C(o, function(v) {
		if (!k && v == f) {
			k = h;
			if (g == f)
				m = h
		} else if (!m && v == g)
			m = h;
		else
			e |= this.Ah(v)
	}, this);
	a.l = e;
	if (!k) {
		o.push(f);
		if (g == f)
			m = h
	}
	m || o.push(g);
	var q = a.mb;
	q && o.push.apply(o, q);
	if (E && !H(Tm)) {
		var x = Um(this, o);
		if (x.length > 0) {
			o.push.apply(o, x);
			c = h
		}
	}
	if (!k || !m || q || c)
		b.className = o.join(I);
	return b
};
l.ka = function(a) {
	zl(a) && this.Xd(a.b(), h);
	a.ea() && this.Xb(a, a.V())
};
l.Ud = function(a, b) {
	Tg(a, !b, !E && !Ac)
};
var Wm = "-rtl";
l.Xd = function(a, b) {
	this.vd(a, this.o() + Wm, b)
};
l.nc = function(a) {
	var b;
	if (a.za & 32 && (b = a.ma()))
		return Jd(b);
	return j
};
var Xm = "tabIndex";
l.Xb = function(a, b) {
	var c;
	if (a.za & 32 && (c = a.ma())) {
		if (!b && a.l & 32) {
			try {
				c.blur()
			} catch (e) {
			}
			a.l & 32 && a.Kc(i)
		}
		if (Jd(c) != b) {
			c = c;
			if (b)
				c.tabIndex = 0;
			else
				c.removeAttribute(Xm)
		}
	}
};
l.A = function(a, b) {
	U(a, b)
};
l.qa = function(a, b, c) {
	var e = a.b();
	if (e) {
		var f = this.Be(b);
		f && this.vd(a, f, c);
		this.ce(e, b, c)
	}
};
var Ym = "disabled", Zm = "pressed", $m = "selected", an = "checked", bn = "expanded";
l.ce = function(a, b, c) {
	Sm || (Sm = Jb(1, Ym, 4, Zm, 8, $m, 16, an, 64, bn));
	(b = Sm[b]) && X(a, b, c)
};
var cn = "nodeType";
l.$a = function(a, b) {
	var c = this.Q(a);
	if (c) {
		wd(c);
		if (b)
			if (u(b))
				Ed(c, b);
			else {
				var e = function(f) {
					if (f) {
						var g = Vc(c);
						c.appendChild(u(f) ? g.createTextNode(f) : f)
					}
				};
				if (t(b))
					C(b, e);
				else
					na(b) && !(cn in b) ? C(ob(b), e) : e(b)
			}
	}
};
l.ma = function(a) {
	return a.b()
};
var dn = "goog-control";
l.o = function() {
	return dn
};
l.kc = function(a) {
	var b = this.o(), c = [ b ], e = this.o();
	e != b && c.push(e);
	b = a.l;
	for (e = []; b;) {
		var f = b & -b;
		e.push(this.Be(f));
		b &= ~f
	}
	c.push.apply(c, e);
	(a = a.mb) && c.push.apply(c, a);
	E && !H(Tm) && c.push.apply(c, Um(this, c));
	return c
};
var en = "_";
function Um(a, b, c) {
	var e = [];
	if (c)
		b = b.concat([ c ]);
	C([], function(f) {
		if (ib(f, xa(kb, b)) && (!c || kb(f, c)))
			e.push(f.join(en))
	});
	return e
}
l.Be = function(a) {
	this.rf || fn(this);
	return this.rf[a]
};
l.Ah = function(a) {
	if (!this.wl) {
		this.rf || fn(this);
		var b = this.rf, c = {}, e;
		for (e in b)
			c[b[e]] = e;
		this.wl = c
	}
	a = parseInt(this.wl[a], 10);
	return isNaN(a) ? 0 : a
};
var gn = "-disabled", hn = "-hover", jn = "-active", kn = "-selected", ln = "-checked", mn = "-focused", nn = "-open";
function fn(a) {
	var b = a.o();
	a.rf = Jb(1, b + gn, 2, b + hn, 4, b + jn, 8, b + kn, 16, b + ln, 32, b
			+ mn, 64, b + nn)
};
function on() {
}
z(on, Rm);
r(on);
l = on.prototype;
l.nb = function() {
	return $l
};
l.ce = function(a, b, c) {
	b == 16 ? X(a, Zm, c) : on.a.ce.call(this, a, b, c)
};
l.d = function(a) {
	var b = on.a.d.call(this, a), c = a.Gd();
	c && this.Mi(b, c);
	(c = a.M()) && this.Ma(b, c);
	a.za & 16 && this.ce(b, 16, j);
	return b
};
l.G = function(a, b) {
	b = on.a.G.call(this, a, b);
	var c = this.M(b);
	a.Ba = c;
	a.Ri = this.Gd(b);
	a.za & 16 && this.ce(b, 16, j);
	return b
};
l.M = p;
l.Ma = p;
l.Gd = function(a) {
	return a.title
};
l.Mi = function(a, b) {
	if (a)
		a.title = b || A
};
var pn = "goog-button";
l.o = function() {
	return pn
};
function qn(a, b) {
	a && rn(this, a, b)
}
z(qn, Q);
l = qn.prototype;
l.m = i;
l.fg = i;
l.bi = i;
l.gg = i;
l.rc = -1;
l.pc = -1;
var sn = {
	"3" : 13,
	"12" : 144,
	"63232" : 38,
	"63233" : 40,
	"63234" : 37,
	"63235" : 39,
	"63236" : 112,
	"63237" : 113,
	"63238" : 114,
	"63239" : 115,
	"63240" : 116,
	"63241" : 117,
	"63242" : 118,
	"63243" : 119,
	"63244" : 120,
	"63245" : 121,
	"63246" : 122,
	"63247" : 123,
	"63248" : 44,
	"63272" : 46,
	"63273" : 36,
	"63275" : 35,
	"63276" : 33,
	"63277" : 34,
	"63289" : 144,
	"63302" : 45
}, tn = {
	Up : 38,
	Down : 40,
	Left : 37,
	Right : 39,
	Enter : 13,
	F1 : 112,
	F2 : 113,
	F3 : 114,
	F4 : 115,
	F5 : 116,
	F6 : 117,
	F7 : 118,
	F8 : 119,
	F9 : 120,
	F10 : 121,
	F11 : 122,
	F12 : 123,
	"U+007F" : 46,
	Home : 36,
	End : 35,
	PageUp : 33,
	PageDown : 34,
	Insert : 45
}, un = {
	61 : 187,
	59 : 186
}, vn = E || G && H(Ug);
l = qn.prototype;
l.ln = function(a) {
	if (G && (this.rc == 17 && !a.ctrlKey || this.rc == 18 && !a.altKey))
		this.pc = this.rc = -1;
	if (vn && !Vg(a.keyCode, this.rc, a.shiftKey, a.ctrlKey, a.altKey))
		this.handleEvent(a);
	else
		this.pc = F && a.keyCode in un ? un[a.keyCode] : a.keyCode
};
l.mn = function() {
	this.pc = this.rc = -1
};
l.handleEvent = function(a) {
	var b = a.Ja, c, e;
	if (E && a.type == Ae) {
		c = this.pc;
		e = c != 13 && c != 27 ? b.keyCode : 0
	} else if (G && a.type == Ae) {
		c = this.pc;
		e = b.charCode >= 0 && b.charCode < 63232 && Wg(c) ? b.charCode : 0
	} else if (Ac) {
		c = this.pc;
		e = Wg(c) ? b.keyCode : 0
	} else {
		c = b.keyCode || this.pc;
		e = b.charCode || 0;
		if (tc && e == 63 && !c)
			c = 191
	}
	var f = c, g = b.keyIdentifier;
	if (c)
		if (c >= 63232 && c in sn)
			f = sn[c];
		else {
			if (c == 25 && a.shiftKey)
				f = 9
		}
	else if (g && g in tn)
		f = tn[g];
	a = f == this.rc;
	this.rc = f;
	b = new wn(f, e, a, b);
	try {
		this.dispatchEvent(b)
	} finally {
		b.u()
	}
};
l.b = function() {
	return this.m
};
var xn = "keyup";
function rn(a, b, c) {
	a.gg && a.detach();
	a.m = b;
	a.fg = O(a.m, Ae, a, c);
	a.bi = O(a.m, Wl, a.ln, c, a);
	a.gg = O(a.m, xn, a.mn, c, a)
}
l.detach = function() {
	if (this.fg) {
		cf(this.fg);
		cf(this.bi);
		cf(this.gg);
		this.gg = this.bi = this.fg = i
	}
	this.m = i;
	this.pc = this.rc = -1
};
l.h = function() {
	qn.a.h.call(this);
	this.detach()
};
var yn = "key";
function wn(a, b, c, e) {
	e && this.Ge(e, void 0);
	this.type = yn;
	this.keyCode = a;
	this.charCode = b;
	this.repeat = c
}
z(wn, we);
function zn(a, b) {
	if (!a)
		d(Error("Invalid class name " + a));
	if (!pa(b))
		d(Error("Invalid decorator function " + b));
	An[a] = b
}
var Bn = {}, An = {};
function $(a, b, c) {
	Y.call(this, c);
	if (!(b = b)) {
		b = this.constructor;
		for ( var e; b;) {
			e = ra(b);
			if (e = Bn[e])
				break;
			b = b.a ? b.a.constructor : i
		}
		b = e ? pa(e.O) ? e.O() : new e : i
	}
	this.q = b;
	this.Ca = a
}
z($, Y);
l = $.prototype;
l.Ca = i;
l.l = 0;
l.za = 39;
l.Ng = 255;
l.yg = 0;
l.aa = h;
l.mb = i;
l.Fh = h;
l.pf = j;
function Cn(a, b) {
	a.C && b != a.Fh && Dn(a, b);
	a.Fh = b
}
l.ma = function() {
	return this.q.ma(this)
};
l.Qf = function() {
	return this.La || (this.La = new qn)
};
function En(a, b) {
	if (b) {
		if (a.mb)
			kb(a.mb, b) || a.mb.push(b);
		else
			a.mb = [ b ];
		a.q.vd(a, b, h)
	}
}
function Fn(a, b) {
	if (b && a.mb) {
		mb(a.mb, b);
		if (a.mb.length == 0)
			a.mb = i;
		a.q.vd(a, b, j)
	}
}
l.vd = function(a, b) {
	b ? En(this, a) : Fn(this, a)
};
l.d = function() {
	var a = this.q.d(this);
	this.m = a;
	var b = this.q.nb();
	b && Fk(a, b);
	this.pf || this.q.Ud(a, j);
	this.V() || this.q.A(a, j)
};
l.Q = function() {
	return this.q.Q(this.b())
};
l.Ha = function(a) {
	return this.q.Ha(a)
};
l.Ua = function(a) {
	this.m = a = this.q.G(this, a);
	var b = this.q.nb();
	b && Fk(a, b);
	this.pf || this.q.Ud(a, j);
	this.aa = a.style.display != Dg
};
l.B = function() {
	$.a.B.call(this);
	this.q.ka(this);
	if (this.za & -2) {
		this.Fh && Dn(this, h);
		if (this.za & 32) {
			var a = this.ma();
			if (a) {
				var b = this.Qf();
				rn(b, a);
				this.ja().e(b, yn, this.fb).e(a, Kk, this.Yf).e(a, Ik, this.Kc)
			}
		}
	}
};
var Gn = "dblclick";
function Dn(a, b) {
	var c = a.ja(), e = a.b();
	if (b) {
		c.e(e, ye, a.Hh).e(e, Mk, a.yb).e(e, Qk, a.Ee).e(e, ze, a.Gh);
		E && c.e(e, Gn, a.ik)
	} else {
		c.Aa(e, ye, a.Hh).Aa(e, Mk, a.yb).Aa(e, Qk, a.Ee).Aa(e, ze, a.Gh);
		E && c.Aa(e, Gn, a.ik)
	}
}
l.P = function() {
	$.a.P.call(this);
	this.La && this.La.detach();
	this.V() && this.ea() && this.q.Xb(this, j)
};
l.h = function() {
	$.a.h.call(this);
	if (this.La) {
		this.La.u();
		delete this.La
	}
	delete this.q;
	this.mb = this.Ca = i
};
l.$a = function(a) {
	this.q.$a(this.b(), a);
	this.Ca = a
};
function Vm(a, b) {
	a.Ca = b
}
function Hn(a, b) {
	var c = a.Ca;
	if (!c || u(c))
		return c;
	return (c = t(c) ? gb(c, b).join(A) : Ld(c)) && Ba(c)
}
l.zd = function() {
	return Hn(this, Ld)
};
l.Ze = function(a) {
	this.$a(a)
};
l.Xd = function(a) {
	$.a.Xd.call(this, a);
	var b = this.b();
	b && this.q.Xd(b, a)
};
l.Ud = function(a) {
	this.pf = a;
	var b = this.b();
	b && this.q.Ud(b, a)
};
l.V = function() {
	return this.aa
};
var In = "show", Jn = "hide";
l.A = function(a, b) {
	if (b || this.aa != a && this.dispatchEvent(a ? In : Jn)) {
		var c = this.b();
		c && this.q.A(c, a);
		this.ea() && this.q.Xb(this, a);
		this.aa = a;
		return h
	}
	return j
};
l.ea = function() {
	return !(this.l & 1)
};
l.Cb = function(a) {
	var b = this.X();
	if (!(b && typeof b.ea == s && !b.ea()) && Kn(this, 1, !a)) {
		if (!a) {
			this.setActive(j);
			this.Db(j)
		}
		this.V() && this.q.Xb(this, a);
		this.qa(1, !a)
	}
};
l.Db = function(a) {
	Kn(this, 2, a) && this.qa(2, a)
};
l.Pa = function() {
	return !!(this.l & 4)
};
l.setActive = function(a) {
	Kn(this, 4, a) && this.qa(4, a)
};
l.dg = function() {
	return !!(this.l & 8)
};
l.Li = function(a) {
	Kn(this, 8, a) && this.qa(8, a)
};
l.F = function(a) {
	Kn(this, 64, a) && this.qa(64, a)
};
l.qa = function(a, b) {
	if (this.za & a && b != !!(this.l & a)) {
		this.q.qa(this, a, b);
		this.l = b ? this.l | a : this.l & ~a
	}
};
function Ln(a, b, c) {
	if (a.C && a.l & b && !c)
		d(Error(wl));
	!c && a.l & b && a.qa(b, j);
	a.za = c ? a.za | b : a.za & ~b
}
function Mn(a, b) {
	return !!(a.Ng & b) && !!(a.za & b)
}
function Kn(a, b, c) {
	return !!(a.za & b) && !!(a.l & b) != c
			&& (!(a.yg & b) || a.dispatchEvent(ql(b, c))) && !a.ah
}
var Nn = "enter";
l.Hh = function(a) {
	!On(a, this.b()) && this.dispatchEvent(Nn) && this.ea() && Mn(this, 2)
			&& this.Db(h)
};
var Pn = "leave";
l.Gh = function(a) {
	if (!On(a, this.b()) && this.dispatchEvent(Pn)) {
		Mn(this, 4) && this.setActive(j);
		Mn(this, 2) && this.Db(j)
	}
};
function On(a, b) {
	return !!a.relatedTarget && Bd(b, a.relatedTarget)
}
l.yb = function(a) {
	if (this.ea()) {
		Mn(this, 2) && this.Db(h);
		if (Ce(a, 0)) {
			Mn(this, 4) && this.setActive(h);
			this.q.nc(this) && this.ma().focus()
		}
	}
	!this.pf && Ce(a, 0) && a.preventDefault()
};
l.Ee = function(a) {
	if (this.ea()) {
		Mn(this, 2) && this.Db(h);
		this.Pa() && this.Vb(a) && Mn(this, 4) && this.setActive(j)
	}
};
l.ik = function(a) {
	this.ea() && this.Vb(a)
};
var Qn = "action", Rn = "altKey", Sn = "ctrlKey", Tn = "metaKey", Un = "shiftKey", Vn = "platformModifierKey";
l.Vb = function(a) {
	if (Mn(this, 16)) {
		var b = !(this.l & 16);
		Kn(this, 16, b) && this.qa(16, b)
	}
	Mn(this, 8) && this.Li(h);
	Mn(this, 64) && this.F(!(this.l & 64));
	b = new N(Qn, this);
	if (a)
		for ( var c = [ Rn, Sn, Tn, Un, Vn ], e, f = 0; e = c[f]; f++)
			b[e] = a[e];
	return this.dispatchEvent(b)
};
l.Yf = function() {
	Mn(this, 32) && Kn(this, 32, h) && this.qa(32, h)
};
l.Kc = function() {
	Mn(this, 4) && this.setActive(j);
	Mn(this, 32) && Kn(this, 32, j) && this.qa(32, j)
};
l.fb = function(a) {
	if (this.V() && this.ea() && this.Id(a)) {
		a.preventDefault();
		a.stopPropagation();
		return h
	}
	return j
};
l.Id = function(a) {
	return a.keyCode == 13 && this.Vb(a)
};
if (!pa($))
	d(Error("Invalid component class " + $));
if (!pa(Rm))
	d(Error("Invalid renderer class " + Rm));
var Wn = ra($);
Bn[Wn] = Rm;
zn(dn, function() {
	return new $(i)
});
function Xn() {
}
z(Xn, on);
r(Xn);
l = Xn.prototype;
l.nb = function() {
};
l.d = function(a) {
	Cn(a, j);
	a.Ng &= -256;
	Ln(a, 32, j);
	return a.g().d($l, {
		"class" : this.kc(a).join(I),
		disabled : !a.ea(),
		title : a.Gd() || A,
		value : a.M() || A
	}, a.zd() || A)
};
var Yn = "INPUT", Zn = "submit", $n = "reset";
l.Ha = function(a) {
	return a.tagName == dm || a.tagName == Yn
			&& (a.type == $l || a.type == Zn || a.type == $n)
};
l.G = function(a, b) {
	Cn(a, j);
	a.Ng &= -256;
	Ln(a, 32, j);
	b.disabled && J(b, this.Be(1));
	return Xn.a.G.call(this, a, b)
};
l.ka = function(a) {
	a.ja().e(a.b(), Be, a.Vb)
};
l.Ud = p;
l.Xd = p;
l.nc = function(a) {
	return a.ea()
};
l.Xb = p;
l.qa = function(a, b, c) {
	Xn.a.qa.call(this, a, b, c);
	if ((a = a.b()) && b == 1)
		a.disabled = c
};
l.M = function(a) {
	return a.value
};
l.Ma = function(a, b) {
	if (a)
		a.value = b
};
l.ce = p;
function ao(a, b, c) {
	$.call(this, a, b || Xn.O(), c)
}
z(ao, $);
l = ao.prototype;
l.M = function() {
	return this.Ba
};
l.Ma = function(a) {
	this.Ba = a;
	this.q.Ma(this.b(), a)
};
l.Gd = function() {
	return this.Ri
};
l.Mi = function(a) {
	this.Ri = a;
	this.q.Mi(this.b(), a)
};
l.h = function() {
	ao.a.h.call(this);
	delete this.Ba;
	delete this.Ri
};
l.B = function() {
	ao.a.B.call(this);
	if (this.za & 32) {
		var a = this.ma();
		a && this.ja().e(a, xn, this.Id)
	}
};
l.Id = function(a) {
	if (a.keyCode == 13 && a.type == yn || a.keyCode == 32 && a.type == xn)
		return this.Vb(a);
	return a.keyCode == 32
};
zn(pn, function() {
	return new ao(i)
});
function bo() {
}
z(bo, on);
r(bo);
l = bo.prototype;
var co = "goog-inline-block ";
l.d = function(a) {
	var b = {
		"class" : co + this.kc(a).join(I),
		title : a.Gd() || A
	};
	return a.g().d(L, b, this.pe(a.Ca, a.g()))
};
l.nb = function() {
	return $l
};
l.Q = function(a) {
	return a && a.firstChild.firstChild
};
var eo = "-outer-box", fo = "-inner-box";
l.pe = function(a, b) {
	return b.d(L, co + (this.o() + eo), b.d(L, co + (this.o() + fo), a))
};
l.Ha = function(a) {
	return a.tagName == Tl
};
l.mk = function(a, b) {
	var c = a.g().Cd(b);
	if (c && c.className.indexOf(this.o() + eo) != -1)
		if ((c = a.g().Cd(c)) && c.className.indexOf(this.o() + fo) != -1)
			return h;
	return j
};
var go = "goog-inline-block";
l.G = function(a, b) {
	ho(b, h);
	ho(b, j);
	this.mk(a, b) || b.appendChild(this.pe(b.childNodes, a.g()));
	J(b, go, this.o());
	return bo.a.G.call(this, a, b)
};
var io = "goog-custom-button";
l.o = function() {
	return io
};
function ho(a, b) {
	if (a)
		for ( var c = b ? a.firstChild : a.lastChild, e; c && c.parentNode == a;) {
			e = b ? c.nextSibling : c.previousSibling;
			if (c.nodeType == 3) {
				var f = c.nodeValue;
				if (Ba(f) == A)
					a.removeChild(c);
				else {
					c.nodeValue = b ? f.replace(/^[\s\xa0]+/, A) : f.replace(
							/[\s\xa0]+$/, A);
					break
				}
			} else
				break;
			c = e
		}
};
function jo() {
}
z(jo, bo);
r(jo);
l = jo.prototype;
l.d = jo.a.d;
l.Q = function(a) {
	return a && a.firstChild && a.firstChild.firstChild
			&& a.firstChild.firstChild.firstChild.lastChild
};
var ko = "-pos", lo = "-top-shadow", mo = "\u00a0";
l.pe = function(a, b) {
	var c = this.o();
	return b.d(L, co + (c + eo), b.d(L, co + (c + fo), b.d(L, c + ko, b.d(L, c
			+ lo, mo), b.d(L, c + Jl, a))))
};
l.mk = function(a, b) {
	var c = a.g().Cd(b);
	if (c && c.className.indexOf(this.o() + eo) != -1)
		if ((c = a.g().Cd(c)) && c.className.indexOf(this.o() + fo) != -1)
			if ((c = a.g().Cd(c)) && c.className.indexOf(this.o() + ko) != -1)
				if ((c = a.g().Cd(c))
						&& c.className.indexOf(this.o() + lo) != -1) {
					a.g();
					if ((c = Ad(c.nextSibling))
							&& c.className.indexOf(this.o() + Jl) != -1)
						return h
				}
	return j
};
var no = "goog-imageless-button";
l.o = function() {
	return no
};
zn(no, function() {
	return new ao(i, jo.O())
});
zn("goog-imageless-toggle-button", function() {
	var a = new ao(i, jo.O());
	Ln(a, 16, h);
	return a
});
var oo = "HTML", po = "BODY";
function qo(a, b, c, e, f, g, k, m) {
	var o, q = c.offsetParent;
	if (q) {
		var x = q.tagName == oo || q.tagName == po;
		if (!x || hg(q) != og) {
			o = wg(q);
			x || (o = mc(o, new lc(q.scrollLeft, q.scrollTop)))
		}
	}
	q = Hg(a);
	(x = vg(a))
			&& q.sk(new ag(x.left, x.top, x.right - x.left, x.bottom - x.top));
	x = K(a);
	var v = K(c);
	if (x.D != v.D) {
		var y = x.D.body;
		v = v.D.parentWindow || v.D.defaultView;
		var ga = new lc(0, 0), Zb = md(Vc(y)), qj = y;
		do {
			var Ca;
			if (Zb == v)
				Ca = wg(qj);
			else {
				var lb = qj;
				Ca = new lc;
				if (lb.nodeType == 1)
					if (lb.getBoundingClientRect) {
						var ff = lg(lb);
						Ca.x = ff.left;
						Ca.y = ff.top
					} else {
						ff = Pd(K(lb));
						lb = wg(lb);
						Ca.x = lb.x - ff.x;
						Ca.y = lb.y - ff.y
					}
				else {
					Ca.x = lb.clientX;
					Ca.y = lb.clientY
				}
				Ca = Ca
			}
			Ca = Ca;
			ga.x += Ca.x;
			ga.y += Ca.y
		} while (Zb && Zb != v && (qj = Zb.frameElement) && (Zb = Zb.parent));
		y = mc(ga, wg(y));
		if (E && !ad(x.D))
			y = mc(y, Pd(x));
		q.left += y.x;
		q.top += y.y
	}
	a = (b & 4 && xg(a) ? b ^ 2 : b) & -5;
	b = new lc(a & 2 ? q.left + q.width : q.left, a & 1 ? q.top + q.height
			: q.top);
	if (o)
		b = mc(b, o);
	if (f) {
		b.x += (a & 2 ? -1 : 1) * f.x;
		b.y += (a & 1 ? -1 : 1) * f.y
	}
	var P;
	if (k)
		if ((P = vg(c)) && o) {
			P.top = Math.max(0, P.top - o.y);
			P.right -= o.x;
			P.bottom -= o.y;
			P.left = Math.max(0, P.left - o.x)
		}
	a: {
		o = P;
		f = b.va();
		P = 0;
		a = (e & 4 && xg(c) ? e ^ 2 : e) & -5;
		e = Gg(c);
		m = m ? m.va() : e;
		if (g || a != 0) {
			if (a & 2)
				f.x -= m.width + (g ? g.right : 0);
			else if (g)
				f.x += g.left;
			if (a & 1)
				f.y -= m.height + (g ? g.bottom : 0);
			else if (g)
				f.y += g.top
		}
		if (k) {
			if (o) {
				g = f;
				P = 0;
				if (g.x < o.left && k & 1) {
					g.x = o.left;
					P |= 1
				}
				if (g.x < o.left && g.x + m.width > o.right && k & 16) {
					m.width -= g.x + m.width - o.right;
					P |= 4
				}
				if (g.x + m.width > o.right && k & 1) {
					g.x = Math.max(o.right - m.width, o.left);
					P |= 1
				}
				if (k & 2)
					P |= (g.x < o.left ? 16 : 0)
							| (g.x + m.width > o.right ? 32 : 0);
				if (g.y < o.top && k & 4) {
					g.y = o.top;
					P |= 2
				}
				if (g.y >= o.top && g.y + m.height > o.bottom && k & 32) {
					m.height -= g.y + m.height - o.bottom;
					P |= 8
				}
				if (g.y + m.height > o.bottom && k & 4) {
					g.y = Math.max(o.bottom - m.height, o.top);
					P |= 2
				}
				if (k & 8)
					P |= (g.y < o.top ? 64 : 0)
							| (g.y + m.height > o.bottom ? 128 : 0);
				k = P
			} else
				k = 256;
			P = k;
			if (P & 496) {
				c = P;
				break a
			}
		}
		jg(c, f);
		k = e == m ? h : !e || !m ? j : e.width == m.width
				&& e.height == m.height;
		k || zg(c, m);
		c = P
	}
	return c
};
function ro() {
}
ro.prototype.Ra = function() {
};
function so(a, b) {
	this.element = a;
	this.md = b
}
z(so, ro);
so.prototype.Ra = function(a, b, c) {
	qo(this.element, this.md, a, b, undefined, c)
};
function to(a, b, c) {
	so.call(this, a, b);
	this.Vl = c
}
z(to, so);
to.prototype.Ra = function(a, b, c, e) {
	var f = qo(this.element, this.md, a, b, i, c, 10, e);
	if (f & 496) {
		var g = this.md, k = b;
		if (f & 48) {
			g ^= 2;
			k ^= 2
		}
		if (f & 192) {
			g ^= 1;
			k ^= 1
		}
		f = qo(this.element, g, a, k, i, c, 10, e);
		if (f & 496)
			this.Vl ? qo(this.element, this.md, a, b, i, c, 5, e) : qo(
					this.element, this.md, a, b, i, c, 0, e)
	}
};
function uo(a, b, c, e) {
	to.call(this, a, b, c);
	this.$o = e
}
z(uo, to);
uo.prototype.Ra = function(a, b, c, e) {
	this.$o ? qo(this.element, this.md, a, b, i, c, 33, e) : uo.a.Ra.call(this,
			a, b, c, e)
};
function vo() {
}
z(vo, Rm);
r(vo);
vo.prototype.d = function(a) {
	return a.g().d(L, this.o())
};
var wo = "HR";
vo.prototype.G = function(a, b) {
	if (b.tagName == wo) {
		var c = b;
		b = this.d(a);
		c.parentNode && c.parentNode.insertBefore(b, c);
		yd(c)
	} else
		J(b, this.o());
	return b
};
vo.prototype.$a = function() {
};
var xo = "goog-menuseparator";
vo.prototype.o = function() {
	return xo
};
function yo(a, b) {
	$.call(this, i, a || vo.O(), b);
	Ln(this, 1, j);
	Ln(this, 2, j);
	Ln(this, 4, j);
	Ln(this, 32, j);
	this.l = 1
}
z(yo, $);
var zo = "separator";
yo.prototype.B = function() {
	yo.a.B.call(this);
	Fk(this.b(), zo)
};
zn(xo, function() {
	return new yo
});
function Ao() {
}
r(Ao);
l = Ao.prototype;
l.nb = function() {
};
function Bo(a, b, c) {
	if (b)
		b.tabIndex = c ? 0 : -1
}
l.d = function(a) {
	return a.g().d(L, this.kc(a).join(I))
};
l.Q = function(a) {
	return a
};
l.Ha = function(a) {
	return a.tagName == Tl
};
var Co = "-horizontal", Do = "horizontal", Eo = "-vertical", Fo = "vertical";
l.G = function(a, b) {
	b.id && rl(a, b.id);
	var c = this.o(), e = j, f = Sc(b);
	f && C(f, function(g) {
		if (g == c)
			e = h;
		else if (g)
			if (g == c + gn)
				a.Cb(j);
			else if (g == c + Co)
				a.Hi(Do);
			else
				g == c + Eo && a.Hi(Fo)
	}, this);
	e || J(b, c);
	Go(this, a, this.Q(b));
	return b
};
function Go(a, b, c) {
	if (c)
		for ( var e = c.firstChild, f; e && e.parentNode == c;) {
			f = e.nextSibling;
			if (e.nodeType == 1) {
				var g = a.rh(e);
				if (g) {
					g.m = e;
					b.ea() || g.Cb(j);
					b.Dc(g);
					g.G(e)
				}
			} else if (!e.nodeValue || Ba(e.nodeValue) == A)
				c.removeChild(e);
			e = f
		}
}
l.rh = function(a) {
	a: {
		for ( var b = Sc(a), c = 0, e = b.length; c < e; c++)
			if (a = b[c] in An ? An[b[c]]() : i) {
				a = a;
				break a
			}
		a = i
	}
	return a
};
l.ka = function(a) {
	a = a.b();
	Tg(a, h, F);
	if (E)
		a.hideFocus = h;
	var b = this.nb();
	b && Fk(a, b)
};
l.ma = function(a) {
	return a.b()
};
var Ho = "goog-container";
l.o = function() {
	return Ho
};
l.kc = function(a) {
	var b = this.o(), c = [ b, a.Qa == Do ? b + Co : b + Eo ];
	a.ea() || c.push(b + gn);
	return c
};
function Io(a, b, c) {
	Y.call(this, c);
	this.q = b || Ao.O();
	this.Qa = a || Fo
}
z(Io, Y);
l = Io.prototype;
l.wk = i;
l.La = i;
l.q = i;
l.Qa = i;
l.aa = h;
l.lb = h;
l.lh = h;
l.Ea = -1;
l.oa = i;
l.Ub = j;
l.Yl = j;
l.Po = h;
l.bc = i;
l.ma = function() {
	return this.wk || this.q.ma(this)
};
l.Qf = function() {
	return this.La || (this.La = new qn(this.ma()))
};
l.d = function() {
	this.m = this.q.d(this)
};
l.Q = function() {
	return this.q.Q(this.b())
};
l.Ha = function(a) {
	return this.q.Ha(a)
};
l.Ua = function(a) {
	this.m = this.q.G(this, a);
	if (a.style.display == Dg)
		this.aa = j
};
l.B = function() {
	Io.a.B.call(this);
	xl(this, function(b) {
		b.C && Jo(this, b)
	}, this);
	var a = this.b();
	this.q.ka(this);
	this.A(this.aa, h);
	this.ja().e(this, Nn, this.Bh).e(this, gl, this.Ch).e(this, hl, this.Kh).e(
			this, ol, this.rn).e(this, pl, this.Zm).e(a, Mk, this.yb).e(Vc(a),
			Qk, this.cn).e(a, [ Mk, Qk, ye, ze ], this.Ym);
	this.nc() && Ko(this, h)
};
function Ko(a, b) {
	var c = a.ja(), e = a.ma();
	b ? c.e(e, Kk, a.Yf).e(e, Ik, a.Kc).e(a.Qf(), yn, a.fb) : c.Aa(e, Kk, a.Yf)
			.Aa(e, Ik, a.Kc).Aa(a.Qf(), yn, a.fb)
}
l.P = function() {
	Lo(this, -1);
	this.oa && this.oa.F(j);
	this.Ub = j;
	Io.a.P.call(this)
};
l.h = function() {
	Io.a.h.call(this);
	if (this.La) {
		this.La.u();
		this.La = i
	}
	this.q = this.oa = this.bc = i
};
l.Bh = function() {
	return h
};
var Mo = "activedescendant";
l.Ch = function(a) {
	var b = Bl(this, a.target);
	if (b > -1 && b != this.Ea) {
		var c = Z(this, this.Ea);
		c && c.Db(j);
		this.Ea = b;
		c = Z(this, this.Ea);
		this.Ub && c.setActive(h);
		if (this.Po && this.oa && c != this.oa)
			c.za & 64 ? c.F(h) : this.oa.F(j)
	}
	X(this.b(), Mo, a.target.b().id)
};
l.Kh = function(a) {
	if (a.target == Z(this, this.Ea))
		this.Ea = -1;
	X(this.b(), Mo, A)
};
l.rn = function(a) {
	if ((a = a.target) && a != this.oa && a.X() == this) {
		this.oa && this.oa.F(j);
		this.oa = a
	}
};
l.Zm = function(a) {
	if (a.target == this.oa)
		this.oa = i
};
l.yb = function(a) {
	if (this.lb)
		this.Ub = h;
	var b = this.ma();
	b && Jd(b) ? b.focus() : a.preventDefault()
};
l.cn = function() {
	this.Ub = j
};
l.Ym = function(a) {
	var b;
	a: {
		b = a.target;
		if (this.bc)
			for ( var c = this.b(); b && b !== c;) {
				var e = b.id;
				if (e in this.bc) {
					b = this.bc[e];
					break a
				}
				b = b.parentNode
			}
		b = i
	}
	if (b)
		switch (a.type) {
		case Mk:
			b.yb(a);
			break;
		case Qk:
			b.Ee(a);
			break;
		case ye:
			b.Hh(a);
			break;
		case ze:
			b.Gh(a)
		}
};
l.Yf = function() {
};
l.Kc = function() {
	Lo(this, -1);
	this.Ub = j;
	this.oa && this.oa.F(j)
};
l.fb = function(a) {
	if (this.ea() && this.V() && (yl(this) != 0 || this.wk) && this.Id(a)) {
		a.preventDefault();
		a.stopPropagation();
		return h
	}
	return j
};
l.Id = function(a) {
	var b = Z(this, this.Ea);
	if (b && typeof b.fb == s && b.fb(a))
		return h;
	if (this.oa && this.oa != b && typeof this.oa.fb == s && this.oa.fb(a))
		return h;
	if (a.shiftKey || a.ctrlKey || a.metaKey || a.altKey)
		return j;
	switch (a.keyCode) {
	case 27:
		if (this.nc())
			this.ma().blur();
		else
			return j;
		break;
	case 36:
		No(this);
		break;
	case 35:
		Oo(this);
		break;
	case 38:
		if (this.Qa == Fo)
			Po(this);
		else
			return j;
		break;
	case 37:
		if (this.Qa == Do)
			zl(this) ? Qo(this) : Po(this);
		else
			return j;
		break;
	case 40:
		if (this.Qa == Fo)
			Qo(this);
		else
			return j;
		break;
	case 39:
		if (this.Qa == Do)
			zl(this) ? Po(this) : Qo(this);
		else
			return j;
		break;
	default:
		return j
	}
	return h
};
function Jo(a, b) {
	var c = b.b();
	c = c.id || (c.id = b.k());
	if (!a.bc)
		a.bc = {};
	a.bc[c] = b
}
l.Dc = function(a, b) {
	Io.a.Dc.call(this, a, b)
};
l.$b = function(a, b, c) {
	a.yg |= 2;
	a.yg |= 64;
	if (this.nc() || !this.Yl)
		Ln(a, 32, j);
	Cn(a, j);
	Io.a.$b.call(this, a, b, c);
	c && this.C && Jo(this, a);
	b <= this.Ea && this.Ea++
};
l.removeChild = function(a, b) {
	if (a = u(a) ? ul(this, a) : a) {
		var c = Bl(this, a);
		if (c != -1)
			if (c == this.Ea)
				a.Db(j);
			else
				c < this.Ea && this.Ea--;
		(c = a.b()) && c.id && Eb(this.bc, c.id)
	}
	a = Io.a.removeChild.call(this, a, b);
	Cn(a, h);
	return a
};
l.Hi = function(a) {
	if (this.b())
		d(Error(wl));
	this.Qa = a
};
l.V = function() {
	return this.aa
};
var Ro = "aftershow";
l.A = function(a, b) {
	if (b || this.aa != a && this.dispatchEvent(a ? In : Jn)) {
		this.aa = a;
		var c = this.b();
		if (c) {
			U(c, a);
			this.nc() && Bo(this.q, this.ma(), this.lb && this.aa);
			b || this.dispatchEvent(this.aa ? Ro : Yl)
		}
		return h
	}
	return j
};
l.ea = function() {
	return this.lb
};
l.Cb = function(a) {
	if (this.lb != a && this.dispatchEvent(a ? fl : el)) {
		if (a) {
			this.lb = h;
			xl(this, function(b) {
				if (b.Kl)
					delete b.Kl;
				else
					b.Cb(h)
			})
		} else {
			xl(this, function(b) {
				if (b.ea())
					b.Cb(j);
				else
					b.Kl = h
			});
			this.Ub = this.lb = j
		}
		this.nc() && Bo(this.q, this.ma(), a && this.aa)
	}
};
l.nc = function() {
	return this.lh
};
l.Xb = function(a) {
	a != this.lh && this.C && Ko(this, a);
	this.lh = a;
	this.lb && this.aa && Bo(this.q, this.ma(), a)
};
function Lo(a, b) {
	var c = Z(a, b);
	if (c)
		c.Db(h);
	else
		a.Ea > -1 && Z(a, a.Ea).Db(j)
}
l.Db = function(a) {
	Lo(this, Bl(this, a))
};
function No(a) {
	So(a, function(b, c) {
		return (b + 1) % c
	}, yl(a) - 1)
}
function Oo(a) {
	So(a, function(b, c) {
		b--;
		return b < 0 ? c - 1 : b
	}, 0)
}
function Qo(a) {
	So(a, function(b, c) {
		return (b + 1) % c
	}, a.Ea)
}
function Po(a) {
	So(a, function(b, c) {
		b--;
		return b < 0 ? c - 1 : b
	}, a.Ea)
}
function So(a, b, c) {
	c = c < 0 ? Bl(a, a.oa) : c;
	var e = yl(a);
	c = b.call(a, c, e);
	for ( var f = 0; f <= e;) {
		var g = Z(a, c);
		if (g && a.lj(g)) {
			Lo(a, c);
			return h
		}
		f++;
		c = b.call(a, c, e)
	}
	return j
}
l.lj = function(a) {
	return a.V() && a.ea() && !!(a.za & 2)
};
function To() {
}
z(To, Rm);
r(To);
var Uo = "goog-menuheader";
To.prototype.o = function() {
	return Uo
};
function Vo(a, b, c) {
	$.call(this, a, c || To.O(), b);
	Ln(this, 1, j);
	Ln(this, 2, j);
	Ln(this, 4, j);
	Ln(this, 32, j);
	this.l = 1
}
z(Vo, $);
zn(Uo, function() {
	return new Vo(i)
});
function Wo() {
	this.Tg = []
}
z(Wo, Rm);
r(Wo);
var Xo = "-highlight", Yo = "-checkbox";
function Zo(a, b) {
	var c = a.Tg[b];
	if (!c) {
		switch (b) {
		case 0:
			c = a.o() + Xo;
			break;
		case 1:
			c = a.o() + Yo;
			break;
		case 2:
			c = a.o() + Jl
		}
		a.Tg[b] = c
	}
	return c
}
l = Wo.prototype;
var $o = "menuitem";
l.nb = function() {
	return $o
};
l.d = function(a) {
	var b = a.g().d(L, this.kc(a).join(I), ap(this, a.Ca, a.g()));
	bp(this, a, b, !!(a.za & 8) || !!(a.za & 16));
	return b
};
l.Q = function(a) {
	return a && a.firstChild
};
var cp = "goog-option";
l.G = function(a, b) {
	var c = zd(b), e = Zo(this, 2);
	c && c.className.indexOf(e) != -1
			|| b.appendChild(ap(this, b.childNodes, a.g()));
	if (kb(Sc(b), cp)) {
		a.vg(h);
		this.vg(a, b, h)
	}
	return Wo.a.G.call(this, a, b)
};
l.$a = function(a, b) {
	var c = this.Q(a), e = dp(this, a) ? c.firstChild : i;
	Wo.a.$a.call(this, a, b);
	if (e && !dp(this, a))
		c.insertBefore(e, c.firstChild || i)
};
function ap(a, b, c) {
	a = Zo(a, 2);
	return c.d(L, a, b)
}
var ep = "menuitemcheckbox";
l.vg = function(a, b, c) {
	if (b) {
		Fk(b, c ? ep : this.nb());
		bp(this, a, b, c)
	}
};
function dp(a, b) {
	var c = a.Q(b);
	if (c) {
		c = c.firstChild;
		var e = Zo(a, 1);
		return !!c && !!c.className && c.className.indexOf(e) != -1
	}
	return j
}
function bp(a, b, c, e) {
	if (e != dp(a, c)) {
		e ? J(c, cp) : Tc(c, cp);
		c = a.Q(c);
		if (e) {
			a = Zo(a, 1);
			c.insertBefore(b.g().d(L, a), c.firstChild || i)
		} else
			c.removeChild(c.firstChild)
	}
}
var fp = "goog-option-selected";
l.Be = function(a) {
	switch (a) {
	case 2:
		return Zo(this, 0);
	case 16:
	case 8:
		return fp;
	default:
		return Wo.a.Be.call(this, a)
	}
};
l.Ah = function(a) {
	var b = Zo(this, 0);
	switch (a) {
	case fp:
		return 16;
	case b:
		return 2;
	default:
		return Wo.a.Ah.call(this, a)
	}
};
var gp = "goog-menuitem";
l.o = function() {
	return gp
};
function hp(a, b, c, e) {
	$.call(this, a, e || Wo.O(), c);
	this.Ma(b)
}
z(hp, $);
hp.prototype.M = function() {
	var a = this.Rc;
	return a != i ? a : this.zd()
};
hp.prototype.Ma = function(a) {
	this.Rc = a
};
hp.prototype.vg = function(a) {
	Ln(this, 16, a);
	var b = this.b();
	b && this.q.vg(this, b, a)
};
var ip = "goog-menuitem-accel";
hp.prototype.zd = function() {
	return Hn(this, function(a) {
		return kb(Sc(a), ip) ? A : Ld(a)
	})
};
zn(gp, function() {
	return new hp(i)
});
function jp() {
}
z(jp, Ao);
r(jp);
l = jp.prototype;
var kp = "menu";
l.nb = function() {
	return kp
};
var lp = "UL";
l.Ha = function(a) {
	return a.tagName == lp || jp.a.Ha.call(this, a)
};
l.rh = function(a) {
	return a.tagName == wo ? new yo : jp.a.rh.call(this, a)
};
l.Lb = function(a, b) {
	return Bd(a.b(), b)
};
var mp = "goog-menu";
l.o = function() {
	return mp
};
var np = "haspopup";
l.ka = function(a) {
	jp.a.ka.call(this, a);
	a = a.b();
	X(a, np, ac)
};
zn(xo, function() {
	return new yo
});
function op(a, b) {
	Io.call(this, Fo, b || jp.O(), a);
	this.Xb(j)
}
z(op, Io);
l = op.prototype;
l.Kg = h;
l.Zl = j;
l.o = function() {
	return this.q.o()
};
l.Lb = function(a) {
	if (this.q.Lb(this, a))
		return h;
	for ( var b = 0, c = yl(this); b < c; b++) {
		var e = Z(this, b);
		if (typeof e.Lb == s && e.Lb(a))
			return h
	}
	return j
};
l.ac = function(a) {
	this.Dc(a, h)
};
l.fd = function(a, b) {
	this.$b(a, b, h)
};
l.removeItem = function(a) {
	(a = this.removeChild(a, h)) && a.u()
};
l.Qd = function(a) {
	(a = this.removeChild(Z(this, a), h)) && a.u()
};
l.Dd = function(a) {
	return Z(this, a)
};
l.th = function() {
	return yl(this)
};
l.A = function(a, b) {
	var c = op.a.A.call(this, a, b);
	c && a && this.C && this.Kg && this.ma().focus();
	return c
};
l.Bh = function(a) {
	this.Kg && this.ma().focus();
	return op.a.Bh.call(this, a)
};
l.lj = function(a) {
	return (this.Zl || a.ea()) && a.V() && !!(a.za & 2)
};
l.Ua = function(a) {
	for ( var b = this.q, c = Od(this.g(), L, b.o() + Jl, a), e, f = 0; e = c[f]; f++)
		Go(b, this, e);
	op.a.Ua.call(this, a)
};
function pp() {
}
z(pp, bo);
r(pp);
if (F)
	pp.prototype.$a = function(a, b) {
		var c = pp.a.Q.call(this, a && a.firstChild);
		if (c) {
			var e = this.createCaption(b, K(a)), f = c.parentNode;
			f && f.replaceChild(e, c)
		}
	};
l = pp.prototype;
l.Q = function(a) {
	a = pp.a.Q.call(this, a && a.firstChild);
	if (F && a && a.__goog_wrapper_div)
		a = a.firstChild;
	return a
};
l.G = function(a, b) {
	var c = Xc(Zc, mp, b)[0];
	if (c) {
		U(c, j);
		Vc(c).body.appendChild(c);
		var e = new op;
		e.G(c);
		a.$e(e)
	}
	return pp.a.G.call(this, a, b)
};
var qp = "-dropdown";
l.pe = function(a, b) {
	return pp.a.pe.call(this, [ this.createCaption(a, b),
			b.d(L, co + (this.o() + qp), mo) ], b)
};
var rp = "-caption";
l.createCaption = function(a, b) {
	return b.d(L, co + (this.o() + rp), a)
};
var sp = "goog-menu-button";
l.o = function() {
	return sp
};
function tp(a, b, c, e) {
	ao.call(this, a, c || pp.O(), e);
	Ln(this, 64, h);
	b && this.$e(b);
	this.$ = new kh(500)
}
z(tp, ao);
l = tp.prototype;
l.je = h;
l.Bi = j;
l.Wh = j;
l.B = function() {
	tp.a.B.call(this);
	this.z && up(this, this.z, h);
	X(this.b(), np, ac)
};
l.P = function() {
	tp.a.P.call(this);
	if (this.z) {
		this.F(j);
		this.z.P();
		up(this, this.z, j);
		var a = this.z.b();
		a && yd(a)
	}
};
l.h = function() {
	tp.a.h.call(this);
	if (this.z) {
		this.z.u();
		delete this.z
	}
	delete this.To;
	this.$.u()
};
l.yb = function(a) {
	tp.a.yb.call(this, a);
	if (this.Pa()) {
		this.F(!(this.l & 64));
		if (this.z)
			this.z.Ub = !!(this.l & 64)
	}
};
l.Ee = function(a) {
	tp.a.Ee.call(this, a);
	if (this.z && !this.Pa())
		this.z.Ub = j
};
l.Vb = function() {
	this.setActive(j);
	return h
};
l.Xf = function(a) {
	this.z && this.z.V() && !this.Lb(a.target) && this.F(j)
};
l.Lb = function(a) {
	return a && Bd(this.b(), a) || this.z && this.z.Lb(a) || j
};
l.Id = function(a) {
	if (a.keyCode == 32) {
		a.preventDefault();
		if (a.type != xn)
			return j
	} else if (a.type != yn)
		return j;
	if (this.z && this.z.V()) {
		var b = this.z.fb(a);
		if (a.keyCode == 27) {
			this.F(j);
			return h
		}
		return b
	}
	if (a.keyCode == 40 || a.keyCode == 38 || a.keyCode == 32) {
		this.F(h);
		return h
	}
	return j
};
l.Dh = function() {
	this.F(j)
};
l.on = function() {
	this.Pa() || this.F(j)
};
l.Kc = function(a) {
	this.Wh || this.F(j);
	tp.a.Kc.call(this, a)
};
function vp(a) {
	a.z || a.$e(new op(a.g()));
	return a.z || i
}
l.$e = function(a) {
	var b = this.z;
	if (a != b) {
		if (b) {
			this.F(j);
			this.C && up(this, b, j);
			delete this.z
		}
		if (a) {
			this.z = a;
			tl(a, this);
			a.A(j);
			var c = this.Wh;
			(a.Kg = c) && a.Xb(h);
			this.C && up(this, a, h)
		}
	}
	return b
};
l.ac = function(a) {
	vp(this).Dc(a, h)
};
l.fd = function(a, b) {
	vp(this).$b(a, b, h)
};
l.removeItem = function(a) {
	(a = vp(this).removeChild(a, h)) && a.u()
};
l.Qd = function(a) {
	var b = vp(this);
	(a = b.removeChild(Z(b, a), h)) && a.u()
};
l.Dd = function(a) {
	return this.z ? Z(this.z, a) : i
};
l.th = function() {
	return this.z ? yl(this.z) : 0
};
l.A = function(a, b) {
	var c = tp.a.A.call(this, a, b);
	c && !this.V() && this.F(j);
	return c
};
l.Cb = function(a) {
	tp.a.Cb.call(this, a);
	this.ea() || this.F(j)
};
l.F = function(a) {
	tp.a.F.call(this, a);
	if (this.z && !!(this.l & 64) == a) {
		if (a) {
			this.z.C || this.z.Za();
			this.Il = vg(this.b());
			this.ij = Hg(this.b());
			wp(this);
			Lo(this.z, -1)
		} else {
			this.setActive(j);
			this.z.Ub = j;
			if (this.qg != i) {
				this.qg = undefined;
				var b = this.z.b();
				b && zg(b, A, A)
			}
		}
		this.z.A(a);
		b = this.ja();
		var c = a ? b.e : b.Aa;
		c.call(b, Nd(this.g()), Mk, this.Xf, h);
		this.Wh && c.call(b, this.z, Ik, this.on);
		c.call(b, this.$, mh, this.Io);
		a ? this.$.start() : this.$.stop()
	}
};
function wp(a) {
	if (a.z.C) {
		var b = a.To || a.b();
		b = new uo(b, a.je ? 5 : 7, !a.Bi, a.Bi);
		var c = a.z.b();
		if (!a.z.V()) {
			c.style.visibility = Eg;
			U(c, h)
		}
		if (!a.qg && a.Bi)
			a.qg = Gg(c);
		b.Ra(c, a.je ? 4 : 6, i, a.qg);
		if (!a.z.V()) {
			U(c, j);
			c.style.visibility = rg
		}
	}
}
l.Io = function() {
	var a = Hg(this.b()), b = vg(this.b()), c;
	c = this.ij;
	c = c == a ? h : !c || !a ? j : c.left == a.left && c.width == a.width
			&& c.top == a.top && c.height == a.height;
	if (!(c = !c)) {
		c = this.Il;
		c = c == b ? h : !c || !b ? j : c.top == b.top && c.right == b.right
				&& c.bottom == b.bottom && c.left == b.left;
		c = !c
	}
	if (c) {
		this.ij = a;
		this.Il = b;
		wp(this)
	}
};
function up(a, b, c) {
	var e = a.ja();
	c = c ? e.e : e.Aa;
	c.call(e, b, Qn, a.Dh);
	c.call(e, b, gl, a.Ch);
	c.call(e, b, hl, a.Kh)
}
l.Ch = function(a) {
	X(this.b(), Mo, a.target.b().id)
};
l.Kh = function() {
	Z(this.z, this.z.Ea) || X(this.b(), Mo, A)
};
zn(sp, function() {
	return new tp(i)
});
function xp() {
}
z(xp, jo);
r(xp);
xp.prototype.Ze = function(a, b, c) {
	b = [ E ? c.d(L, go, b) : b, c.d(L, co + (this.o() + qp)) ];
	this.$a(a, b)
};
var yp = "goog-imageless-menu-button";
xp.prototype.o = function() {
	return yp
};
zn(yp, function() {
	return new tp(i, undefined, xp.O())
});
function zp(a, b) {
	this.da = new Ck(this);
	Ap(this, a || i);
	if (b)
		this.be = b
}
z(zp, Q);
l = zp.prototype;
l.m = i;
l.am = h;
l.hj = i;
l.Pc = j;
l.ip = j;
l.gi = -1;
l.fi = -1;
l.Ln = j;
l.Bm = h;
var Bp = "toggle_display";
l.be = Bp;
l.b = function() {
	return this.m
};
function Ap(a, b) {
	if (a.Pc)
		d(Error("Can not change this state of the popup while showing."));
	a.m = b
}
l.V = function() {
	return this.Pc
};
function Cp(a) {
	return a.Pc || ya() - a.fi < 150
}
var Dp = "beforeshow", Ep = "IFRAME", Fp = "move_offscreen";
l.A = function(a) {
	if (a) {
		if (!this.Pc)
			if (this.dispatchEvent(Dp)) {
				if (!this.m)
					d(Error("Caller must call setElement before trying to show the popup"));
				this.Ra();
				a = Vc(this.m);
				this.Ln && this.da.e(a, Wl, this.zo, h);
				if (this.am) {
					this.da.e(a, Mk, this.Kk, h);
					if (E) {
						for ( var b = a.activeElement; b && b.nodeName == Ep;) {
							try {
								var c = Cd(b)
							} catch (e) {
								break
							}
							a = c;
							b = a.activeElement
						}
						this.da.e(a, Mk, this.Kk, h);
						this.da.e(a, jl, this.Jk)
					} else
						this.da.e(a, Ik, this.Jk)
				}
				if (this.be == Bp) {
					this.m.style.visibility = rg;
					U(this.m, h)
				} else
					this.be == Fp && this.Ra();
				this.Pc = h;
				this.gi = ya();
				this.fi = -1;
				this.dispatchEvent(In)
			}
	} else
		Gp(this)
};
l.Ra = p;
var Hp = "beforehide", Ip = "-200px";
function Gp(a, b) {
	if (!a.Pc || !a.dispatchEvent({
		type : Hp,
		target : b
	}))
		return j;
	a.da && a.da.Pd();
	if (a.be == Bp)
		a.ip ? V(a.ok, 0, a) : a.ok();
	else if (a.be == Fp) {
		a.m.style.left = Ip;
		a.m.style.top = Ip
	}
	a.Pc = j;
	a.fi = ya();
	a.dispatchEvent({
		type : Jn,
		target : b
	});
	return h
}
l.ok = function() {
	this.m.style.visibility = Eg;
	U(this.m, j)
};
l.Kk = function(a) {
	a = a.target;
	if (!Bd(this.m, a) && (!this.hj || Bd(this.hj, a))
			&& !(ya() - this.gi < 150))
		Gp(this, a)
};
l.zo = function(a) {
	if (a.keyCode == 27)
		if (Gp(this, a.target)) {
			a.preventDefault();
			a.stopPropagation()
		}
};
l.Jk = function(a) {
	if (this.Bm) {
		var b = Vc(this.m);
		if (E || Ac) {
			if ((a = b.activeElement) && Bd(this.m, a))
				return
		} else if (a.target != b)
			return;
		ya() - this.gi < 150 || Gp(this)
	}
};
l.h = function() {
	zp.a.h.call(this);
	this.da.u();
	delete this.m;
	delete this.da
};
function Jp(a, b) {
	this.So = 4;
	this.Tk = b || undefined;
	zp.call(this, a)
}
z(Jp, zp);
Jp.prototype.Ra = function() {
	if (this.Tk) {
		var a = !this.V() && this.be != Fp, b = this.b();
		if (a) {
			b.style.visibility = Eg;
			U(b, h)
		}
		this.Tk.Ra(b, this.So, this.no);
		a && U(b, j)
	}
};
function Kp(a, b, c, e) {
	ao.call(this, a, c || pp.O(), e);
	Ln(this, 64, h);
	a = this.W;
	if (b != a) {
		a && this.F(j);
		(this.W = b) && b.A(j)
	}
}
z(Kp, ao);
l = Kp.prototype;
l.W = i;
l.je = h;
l.P = function() {
	Kp.a.P.call(this);
	this.W && this.F(j)
};
l.h = function() {
	Kp.a.h.call(this);
	if (this.W) {
		this.W.u();
		this.W = i
	}
};
l.yb = function(a) {
	Kp.a.yb.call(this, a);
	this.W && this.Pa() && this.F(!(this.l & 64))
};
l.Vb = function() {
	this.setActive(j);
	return h
};
l.Xf = function(a) {
	this.W && Cp(this.W) && !this.Lb(a.target) && this.F(j)
};
l.Lb = function(a) {
	return a && Bd(this.b(), a) || this.W && this.W.b() && Bd(this.W.b(), a)
};
l.fb = function(a) {
	if (this.W) {
		if (Cp(this.W)) {
			if (a.keyCode == 27) {
				this.F(j);
				return h
			}
			return j
		}
		if (a.keyCode == 40 || a.keyCode == 38 || a.keyCode == 13) {
			this.F(h);
			return h
		}
	}
	return j
};
l.jk = function() {
	this.W && this.F(j)
};
l.A = function(a) {
	Kp.a.A.call(this, a);
	this.W && !this.V() && this.F(j)
};
l.Cb = function(a) {
	Kp.a.Cb.call(this, a);
	this.W && !this.ea() && this.F(j)
};
l.F = function(a) {
	if (a) {
		var b = this.je ? 5 : 7;
		b = new to(this.b(), b);
		var c = this.W.b();
		if (!Cp(this.W)) {
			c.style.visibility = Eg;
			U(c, h)
		}
		b.Ra(c, this.je ? 4 : 6, new $f(0, 0, 0, 0));
		if (!Cp(this.W)) {
			U(c, j);
			c.style.visibility = rg
		}
	}
	Kp.a.F.call(this, a);
	b = this.ja();
	if (a) {
		this.W.A(h);
		b.e(this.W, Qn, this.jk);
		b.e(Nd(this.g()), Mk, this.Xf, h)
	} else {
		this.W.A(j);
		b.Aa(this.W, Qn, this.jk);
		b.Aa(Nd(this.g()), Mk, this.Xf, h)
	}
};
zn("goog-popup-button", function() {
	return new Kp(i)
});
function Lp() {
}
z(Lp, jo);
r(Lp);
var Mp = "goog-imageless-popup-button";
Lp.prototype.o = function() {
	return Mp
};
zn(Mp, function() {
	return new Kp(i, undefined, Lp.O())
});
function Np(a, b, c) {
	this.Le = a;
	this.Tb = b;
	this.da = c;
	this.Rg = w(this.Ko, this)
}
z(Np, Qd);
l = Np.prototype;
l.Zd = j;
l.Se = 0;
l.$ = i;
l.jh = function() {
	if (!this.$ && !this.Se)
		this.qd();
	else
		this.Zd = h
};
l.stop = function() {
	if (this.$) {
		lh.clearTimeout(this.$);
		this.$ = i;
		this.Zd = j
	}
};
l.pause = function() {
	this.Se++
};
l.cl = function() {
	this.Se--;
	if (!this.Se && this.Zd && !this.$) {
		this.Zd = j;
		this.qd()
	}
};
l.h = function() {
	Np.a.h.call(this);
	this.stop()
};
l.Ko = function() {
	this.$ = i;
	if (this.Zd && !this.Se) {
		this.Zd = j;
		this.qd()
	}
};
l.qd = function() {
	this.$ = V(this.Rg, this.Tb);
	this.Le.call(this.da)
};
function Op(a) {
	this.da = a;
	this.Xg = new Np(a.getData, 100, a)
}
z(Op, Qd);
l = Op.prototype;
l.el = j;
l.Zi = i;
l.Ye = i;
l.il = j;
l.Yd = i;
l.zp = 0.5;
l.bf = function(a) {
	this.Zi = a
};
l.hp = function(a) {
	this.il = a
};
l.Ki = function(a) {
	this.el = a
};
l.lk = function(a, b) {
	if (!b) {
		this.il = h;
		if (this.Yd) {
			lh.clearTimeout(this.Yd);
			this.Yd = i
		}
		this.Yd = V(w(this.hp, this, j), 50)
	}
	if (this.da.Pa()) {
		if (this.Ye)
			if (b)
				this.Ye = i;
			else
				return;
		if (this.da.eg.Oe.Df || this.da.eg.xg.Df)
			if (b)
				return;
			else
				this.Ye = V(w(this.lk, this, i, h), 100);
		this.Xg.jh()
	}
};
function Pp(a, b, c, e) {
	if (b >= c)
		return j;
	if (a.el)
		return h;
	b = a.Zi;
	if (e && b) {
		c = wg(e);
		var f = b.height;
		return e.scrollHeight - (b.top - c.y) - f < f * a.zp
	}
	return j
}
l.h = function() {
	lh.clearTimeout(this.Ye);
	this.Ye = i;
	lh.clearTimeout(this.Yd);
	this.Yd = i;
	this.Xg.u();
	delete this.Xg;
	this.Zi = i
};
function Qp() {
}
z(Qp, Qd);
function Rp(a) {
	return a != i && (!u(a) || !Aa(a)) && (!t(a) || a.length != 0)
			&& (!qa(a) || !Db(a))
};
var Sp = "user";
function Tp(a, b, c, e, f, g) {
	this.Ld = a;
	this.na = String(b);
	this.bp = c || Sp;
	this.po = !!e;
	this.Mn = Aa(f) ? i : f;
	this.Zo = !!g
}
var Up = new D;
Tp.prototype.getName = function() {
	return this.Ld
};
Tp.prototype.k = function() {
	return this.na
};
Tp.prototype.va = function(a) {
	var b = this.Ac();
	a && Hb(b, a);
	a = b.scopeType;
	b = Up.Oa(a) ? Up.w(a).call(undefined, b) : new Tp(b.name, b.id,
			b.scopeType, b.me, b.iconUrl, b.requiresKey);
	return b
};
Tp.prototype.Ac = function() {
	var a = {};
	a.iconUrl = this.Mn;
	a.scopeType = this.bp;
	a.name = this.Ld;
	a.id = this.na;
	a.me = this.po;
	a.requiresKey = this.Zo;
	return yb(a, Rp)
};
var Vp = "domain";
function Wp(a, b, c, e, f, g) {
	Tp.call(this, a, b, Vp, e, f, g);
	this.ym = c
}
z(Wp, Tp);
Wp.prototype.Ac = function() {
	var a = Wp.a.Ac.call(this);
	a.domainName = this.ym;
	return yb(a, Rp)
};
Up.t(Vp, function(a) {
	return new Wp(a.name, a.id, a.domainName, a.me, a.iconUrl, a.requiresKey)
});
new Wp(A, "DEFAULT", A, h);
function Xp(a, b, c, e, f, g) {
	this.l = a;
	this.$d = b;
	this.Xc = e || 0;
	this.Wc = f !== undefined ? f : h;
	this.Hj = g || i;
	this.Fj = c || i
}
Xp.prototype.Ac = function() {
	var a = {};
	a.visibilityState = this.l;
	a.summary = this.$d;
	a.role = this.Xc;
	a.restrictedToDomain = this.Wc;
	if (this.Hj)
		a.domainDisplayNames = ob(this.Hj);
	a.details = this.Fj;
	return yb(a, Rp)
};
Xp.prototype.Wf = function() {
	var a = {};
	a.summary = this.$d;
	a.Wp = this.Fj;
	return a
};
function Yp(a, b, c, e, f, g, k) {
	this.l = a;
	this.$d = b;
	this.Xc = c || 0;
	this.Wc = e !== undefined ? e : h;
	this.xk = f || i;
	this.td = g || i;
	this.db = k
			|| [ new Xp(this.l, this.$d, i, this.Xc, this.Wc,
					this.td ? [ this.td ] : undefined) ]
}
var Zp = "private";
function $p(a, b) {
	if (a.l == Zp && b.l == Zp)
		return h;
	return a.l == b.l && a.Xc == b.Xc && a.Wc == b.Wc
}
Yp.prototype.Ac = function() {
	var a = {};
	a.visibilityState = this.l;
	a.summary = this.$d;
	a.role = this.Xc;
	a.restrictedToDomain = this.Wc;
	for ( var b = [], c = 0; c < this.db.length; c++)
		b.push(this.db[c].Ac());
	a.visibilityEntries = b;
	if (this.xk)
		a.key = this.xk;
	if (this.td)
		a.domainDisplayName = this.td;
	return yb(a, Rp)
};
function aq(a) {
	var b, c;
	if (c = a.visibilityEntries) {
		b = [];
		for ( var e = 0; e < c.length; e++)
			b
					.push(new Xp(c[e].visibilityState, c[e].summary,
							c[e].details, c[e].role, c[e].restrictedToDomain,
							c[e].domainDisplayNames))
	}
	return new Yp(a.visibilityState, a.summary, a.role, a.restrictedToDomain,
			a.key, a.domainDisplayName, b)
}
Yp.prototype.Wf = function() {
	var a = {};
	a.state = this.l;
	a.summary = this.$d;
	a.tg = this.Wc;
	if (this.td)
		a.pg = this.td;
	a.Kq = this.Xc;
	a.Cp = be(this.Ac());
	for ( var b = [], c = 0; c < this.db.length; c++)
		b.push(this.db[c].Wf());
	a.eq = b;
	return a
};
function bq() {
}
l = bq.prototype;
l.w = p;
l.t = p;
l.Jc = p;
l.jc = p;
l.ph = p;
l.Nf = p;
l.Of = p;
l.Yh = p;
function cq(a) {
	this.S = {};
	this.Ke = [];
	this.mc = {};
	if (a)
		for ( var b = 0, c; c = a[b]; b++)
			this.add(c)
}
l = cq.prototype;
l.add = function(a) {
	this.Ke.push(a);
	var b = a.Nf();
	if (b) {
		this.S[b] = a;
		this.mc[b] = this.Ke.length - 1
	}
};
l.w = function(a) {
	return this.S[a] || i
};
l.Rb = function() {
	return this.Ke.length
};
l.ql = function(a, b) {
	if (b == i)
		this.removeNode(a);
	else {
		var c = this.mc[a];
		if (c != i) {
			this.S[a] = b;
			this.Ke[c] = b
		} else
			this.add(b)
	}
};
l.removeNode = function(a) {
	var b = this.mc[a];
	if (b != i) {
		this.Ke.splice(b, 1);
		delete this.S[a];
		delete this.mc[a];
		for ( var c in this.mc)
			this.mc[c] > b && this.mc[c]--
	}
	return b != i
};
l.indexOf = function(a) {
	return this.mc[a]
};
function dq() {
	cq.call(this)
}
z(dq, cq);
dq.prototype.add = function() {
	d(Error("Can't add to EmptyNodeList"))
};
function eq(a) {
	a && fq(this, a)
}
var gq = "()", hq = "name()", iq = "count()", jq = "position()", kq = "$", lq = "*|text()", mq = "@*";
function fq(a, b, c, e, f) {
	a.kp = b;
	if (!e && !f) {
		if (za(b, ni)) {
			a.dm = h;
			b = b.substring(0, b.length - 1)
		}
		if (za(b, gq))
			if (za(b, hq) || za(b, iq) || za(b, jq)) {
				e = b.lastIndexOf(R);
				if (e != -1) {
					a.gh = b.substring(e + 1);
					b = b.substring(0, e)
				} else {
					a.gh = b;
					b = ba
				}
				if (a.gh == iq)
					a.Vn = h
			}
	}
	a.Tc = c || b.split(R);
	a.vb = a.Tc.length;
	a.Je = a.Tc[a.vb - 1];
	a.J = a.Tc[0];
	if (a.vb == 1) {
		a.Ai = a;
		a.He = b.lastIndexOf(kq, 0) == 0
	} else {
		a.Ai = nq(a.J, i, a, i);
		a.He = a.Ai.He;
		a.J = a.Ai.J
	}
	if (a.vb == 1 && !a.He) {
		a.Wn = b == ba || b == A;
		a.Yn = b.lastIndexOf(Xd, 0) == 0;
		a.Sn = b == lq;
		a.Rn = b == mq;
		a.Tn = b == Zc
	}
}
l = eq.prototype;
l.X = function() {
	if (!this.Qo) {
		if (this.vb > 1)
			this.Rk = nq(i, this.Tc.slice(0, this.Tc.length - 1), this, i);
		this.Qo = h
	}
	return this.Rk
};
function oq(a) {
	if (!a.vo) {
		if (a.vb > 1)
			a.Fk = nq(i, a.Tc.slice(1), i, a);
		a.vo = h
	}
	return a.Fk
}
l.M = function(a) {
	if (a == i)
		a = pq();
	else if (this.He)
		a = a.qh ? a.qh() : pq();
	if (this.Vn)
		return qq(this, a, j, void 0).Rb();
	if (this.vb == 1)
		return a.ph(this.J);
	else if (this.vb == 0)
		return a.w();
	a = a.jc(this.J);
	return a == i ? i : oq(this).M(a)
};
function qq(a, b, c, e) {
	if (b == i)
		b = pq();
	else if (a.He)
		b = b.qh ? b.qh() : pq();
	if (a.vb == 0 && c)
		return b;
	else if (a.vb == 0 && !c)
		return new cq([ b ]);
	else if (a.vb == 1)
		if (c)
			return b.jc(a.J, e);
		else
			return (c = b.jc(a.J)) && c.Yh() ? c.Jc() : b.Jc(a.J);
	else {
		b = b.jc(a.J, e);
		if (b == i && c)
			return i;
		else if (b == i && !c)
			return new dq;
		return qq(oq(a), b, c, e)
	}
}
l.dm = j;
l.Tc = [];
l.vb = i;
l.Je = i;
l.Wn = j;
l.Yn = j;
l.Sn = j;
l.Rn = j;
l.Tn = j;
l.gh = i;
l.Rk = i;
l.Fk = i;
function rq(a) {
	var b = sq[a];
	if (b == i) {
		b = new eq(a);
		sq[a] = b
	}
	return b
}
function nq(a, b, c, e) {
	a = a || b.join(R);
	var f = sq[a];
	if (f == i) {
		f = new eq;
		fq(f, a, b, c, e);
		sq[a] = f
	}
	return f
}
var sq = {};
rq(ba);
rq(lq);
rq(Zc);
rq(mq);
rq(hq);
rq(iq);
rq(jq);
function tq() {
	this.xf = new cq;
	this.Pp = new D;
	this.mo = {};
	this.wq = {};
	this.of = {};
	this.Em = 0;
	this.rq = {}
}
var uq = i;
z(tq, bq);
function pq() {
	uq || (uq = new tq);
	return uq
}
l = tq.prototype;
l.w = function() {
	return this.xf
};
l.t = function() {
	d(Error("Can't set on DataManager"))
};
l.Jc = function(a) {
	return a ? new cq([ this.jc(a) ]) : this.xf
};
l.jc = function(a) {
	return this.of[a] ? qq(this.of[a], void 0, h, void 0) : this.xf.w(a)
};
l.ph = function(a) {
	return (a = this.of[a] ? qq(this.of[a], void 0, h, void 0) : this.xf.w(a)) ? a
			.w()
			: i
};
l.Nf = function() {
	return A
};
l.Of = function() {
	return A
};
l.Yh = function() {
	return j
};
function vq(a, b, c) {
	this.pa = c;
	this.Wg = b;
	this.J = a;
	this.cc = i
}
l = vq.prototype;
l.w = function() {
	return !qa(this.J) ? this.J : this.Jc()
};
l.t = function(a) {
	if (a && qa(this.J))
		d(Error("Can't set group nodes to new values yet"));
	if (this.pa)
		this.pa.J[this.Wg] = a;
	this.J = a;
	this.cc = i;
	a = pq();
	var b = this.Of();
	if (!a.Xp) {
		for ( var c = rq(b), e = 0; c;) {
			var f = a.mo[c.kp];
			if (f)
				for ( var g in f) {
					var k = f[g], m = k.Qc;
					e <= k.xq && m.fq(b, m.id)
				}
			e++;
			c = c.X()
		}
		a.Em++
	}
};
l.Jc = function(a) {
	if (!this.J)
		return new dq;
	if (!a || a == Zc) {
		if (!this.cc)
			if (qa(this.J)) {
				a = new cq;
				var b;
				if (t(this.J))
					for ( var c = this.J.length, e = 0; e < c; e++) {
						b = this.J[e];
						var f = b.id;
						f = f != i ? String(f) : ee + e + Yd;
						b = new vq(b, f, this);
						a.add(b)
					}
				else
					for (f in this.J) {
						c = this.J[f];
						if (c.Nf)
							a.add(c);
						else if (!pa(c)) {
							b = new vq(c, f, this);
							a.add(b)
						}
					}
				this.cc = a
			} else
				this.cc = new dq;
		return this.cc
	} else if (a.indexOf(Zc) == -1)
		return this.J[a] != i ? new cq([ this.jc(a) ]) : new dq;
	else
		d(Error("Selector not supported yet (" + a + Zd))
};
l.jc = function(a, b) {
	if (!this.J)
		return i;
	var c = this.Jc().w(a);
	if (!c && b) {
		c = {};
		if (t(this.J)) {
			c.id = a;
			this.J.push(c)
		} else
			this.J[a] = c;
		c = new vq(c, a, this);
		this.cc && this.cc.add(c)
	}
	return c
};
l.ph = function(a) {
	if (this.cc)
		return (a = this.Jc().w(a)) ? a.w() : i;
	else
		return this.J ? this.J[a] : i
};
l.Nf = function() {
	return this.Wg
};
l.Of = function() {
	var a = A;
	if (this.pa)
		a = this.pa.Of() + R;
	return a + this.Wg
};
l.Yh = function() {
	return this.Zn != i ? this.Zn : t(this.J)
};
function wq(a, b, c) {
	vq.call(this, a, b, c)
}
z(wq, vq);
Hb(wq.prototype, Qd.prototype);
wq.prototype.io = i;
wq.prototype.h = function() {
	Qd.prototype.h.call(this);
	this.t(i);
	this.io = i
};
function xq(a) {
	Y.call(this, a)
}
z(xq, Y);
l = xq.prototype;
l.N = i;
l.Jg = i;
l.Xl = h;
l.uh = function() {
	return this.Xl ? this.X() : i
};
l.Ua = function(a) {
	xq.a.Ua.call(this, a);
	a.id && rl(this, a.id)
};
l.P = function() {
	xq.a.P.call(this);
	this.N && this.N.Pd()
};
l.h = function() {
	xq.a.h.call(this);
	if (this.N) {
		this.N.u();
		this.N = i
	}
	if (this.Jg) {
		this.Jg.u();
		this.Jg = i
	}
};
var yq = "Cache-Control", zq = "no-cache", Aq = "application/x-www-form-urlencoded", Bq = "X-Same-Domain", Cq = "explorer";
function Dq(a, b, c, e) {
	var f = Dh();
	try {
		if (a != i) {
			f.open(Oh, a, h);
			f.setRequestHeader(yq, zq);
			f.setRequestHeader(Ph, Aq);
			f.setRequestHeader(Bq, Cq);
			if (e)
				f.onreadystatechange = w(e, c, f);
			f.send(b)
		}
	} catch (g) {
		return i
	}
	return f
}
var Eq = "&&&START&&&";
function Fq(a) {
	a = a.responseText;
	if (a.indexOf(Eq) == 0)
		a = a.substring(11);
	return eval(Xb + a + Zd)
};
var Gq = F && tc && H(1.9);
function Hq(a) {
	if (a.type == Mk && (Ce(a, 2) || G && tc && Ce(a, 0) && a.ctrlKey)) {
		Gq || a.preventDefault();
		return h
	}
	return j
};
function Iq(a, b, c) {
	this.Le = a;
	this.Tb = b || 0;
	this.da = c;
	this.Rg = w(this.qd, this)
}
z(Iq, Qd);
l = Iq.prototype;
l.na = 0;
l.h = function() {
	Iq.a.h.call(this);
	this.stop();
	delete this.Le;
	delete this.da
};
l.start = function(a) {
	this.stop();
	this.na = V(this.Rg, a !== undefined ? a : this.Tb)
};
l.stop = function() {
	this.Pa() && lh.clearTimeout(this.na);
	this.na = 0
};
l.jh = function() {
	this.stop();
	this.qd()
};
l.Pa = function() {
	return this.na != 0
};
l.qd = function() {
	this.na = 0;
	this.Le && this.Le.call(this.da)
};
function Jq(a, b, c) {
	Y.call(this, c);
	this.j = b || Kq;
	this.pk = a
}
z(Jq, Y);
var Lq = {};
l = Jq.prototype;
l.Di = j;
l.ye = j;
l.sp = i;
l.Wl = A;
l.Ie = h;
l.Af = -1;
l.h = function() {
	Jq.a.h.call(this);
	if (this.Bc) {
		this.Bc.removeNode(this);
		this.Bc = i
	}
	this.m = i
};
var Mq = ".label", Nq = "treeitem", Oq = "level", Pq = "presentation", Qq = "group", Rq = "setsize", Sq = "posinset";
l.bg = function() {
	var a = this.b();
	if (a) {
		var b = Tq(this);
		if (b && !b.id)
			b.id = this.k() + Mq;
		Fk(a, Nq);
		X(a, $m, j);
		X(a, bn, j);
		X(a, Oq, this.Bd());
		b && X(a, Ml, b.id);
		(a = this.Pf()) && Fk(a, Pq);
		(a = this.sh()) && Fk(a, Pq);
		a = Uq(this);
		Fk(a, Qq);
		if (a.hasChildNodes()) {
			a = yl(this);
			for (b = 1; b <= a; b++) {
				var c = Z(this, b - 1).b();
				X(c, Rq, a);
				X(c, Sq, b)
			}
		}
	}
};
l.d = function() {
	var a = new M;
	this.gf(a);
	var b = this.g();
	this.m = ud(b.D, a.toString())
};
l.B = function() {
	Jq.a.B.call(this);
	Lq[this.k()] = this;
	this.bg()
};
l.P = function() {
	Jq.a.P.call(this);
	delete Lq[this.k()]
};
l.$b = function(a, b) {
	a.X();
	var c = Z(this, b - 1), e = Z(this, b);
	Jq.a.$b.call(this, a, b);
	a.uc = c;
	a.gb = e;
	if (c)
		c.gb = a;
	else
		this.Uj = a;
	if (e)
		e.uc = a;
	else
		this.yk = a;
	var f = this.xa();
	f && Vq(a, f);
	Wq(a, this.Bd() + 1);
	if (this.b()) {
		this.ee();
		if (this.wa()) {
			f = Uq(this);
			a.b() || a.d();
			var g = a.b(), k = e && e.b();
			f.insertBefore(g, k);
			this.C && a.B();
			if (!e)
				if (c)
					c.ee();
				else {
					U(f, h);
					this.Sa(this.wa())
				}
		}
	}
};
l.add = function(a, b) {
	!b || b.X();
	a.X() && a.X().removeChild(a);
	this.$b(a, b ? Bl(this, b) : yl(this));
	return a
};
l.removeChild = function(a) {
	var b = this.xa(), c = b ? b.ob() : i;
	if (c == a || a.contains(c))
		if (b.hasFocus()) {
			this.select();
			V(this.Jo, 10, this)
		} else
			this.select();
	Jq.a.removeChild.call(this, a);
	if (this.yk == a)
		this.yk = a.uc;
	if (this.Uj == a)
		this.Uj = a.gb;
	if (a.uc)
		a.uc.gb = a.gb;
	if (a.gb)
		a.gb.uc = a.uc;
	c = !a.gb;
	a.Bc = i;
	a.Af = -1;
	if (b) {
		b.removeNode(this);
		if (this.C) {
			b = Uq(this);
			if (a.C) {
				var e = a.b();
				b.removeChild(e);
				a.P()
			}
			if (c)
				(c = Z(this, yl(this) - 1)) && c.ee();
			if (!Al(this)) {
				b.style.display = Dg;
				this.ee();
				this.Pf().className = this.Lf()
			}
		}
	}
	return a
};
l.remove = Jq.prototype.removeChild;
l.Jo = function() {
	this.select()
};
l.Bd = function() {
	var a = this.Af;
	if (a < 0) {
		a = (a = this.X()) ? a.Bd() + 1 : 0;
		Wq(this, a)
	}
	return a
};
function Wq(a, b) {
	if (b != a.Af) {
		a.Af = b;
		var c = Xq(a);
		if (c) {
			var e = Yq(a) + Ag;
			if (zl(a))
				c.style.paddingRight = e;
			else
				c.style.paddingLeft = e
		}
		xl(a, function(f) {
			Wq(f, b + 1)
		})
	}
}
l.contains = function(a) {
	for (; a;) {
		if (a == this)
			return h;
		a = a.X()
	}
	return j
};
function Zq(a) {
	var b = [];
	xl(a, function(c) {
		b.push(c)
	});
	return b
}
l.dg = function() {
	return this.Di
};
l.select = function() {
	var a = this.xa();
	a && a.ab(this)
};
function $q(a, b) {
	if (a.Di != b) {
		a.Di = b;
		ar(a);
		var c = a.b();
		if (c) {
			X(c, $m, b);
			b && X(a.xa().b(), Mo, a.k())
		}
	}
}
l.wa = function() {
	return this.ye
};
var br = "beforeexpand", cr = "beforecollapse", dr = "expand", er = "collapse";
l.Sa = function(a) {
	var b = a != this.ye;
	if (b)
		if (!this.dispatchEvent(a ? br : cr))
			return;
	var c;
	this.ye = a;
	c = this.xa();
	var e = this.b();
	if (Al(this)) {
		!a && c && this.contains(c.ob()) && this.select();
		if (e) {
			if (c = Uq(this)) {
				U(c, a);
				if (a && this.C && !c.hasChildNodes()) {
					var f = new M;
					xl(this, function(g) {
						g.gf(f)
					});
					c.innerHTML = f.toString();
					xl(this, function(g) {
						g.B()
					})
				}
			}
			this.ee()
		}
	} else
		(c = Uq(this)) && U(c, j);
	if (e) {
		this.Pf().className = this.Lf();
		X(e, bn, a)
	}
	if (b)
		this.dispatchEvent(a ? dr : er)
};
l.zi = function() {
	var a = this.X();
	if (a) {
		a.Sa(h);
		a.zi()
	}
};
var fr = '<div class="', gr = '" id="', hr = '" style="', ir = "display:none;", jr = "</div></div>";
l.gf = function(a) {
	var b = this.xa();
	b = !b.Ni || b == this.X() && !b.Oi ? this.j.qj : this.j.pj;
	var c = this.wa() && Al(this);
	a.append(fr, this.j.zj, gr, this.k(), wm, this.yh(), fr, b, hr, kr(this),
			c ? A : ir, wm);
	c && xl(this, function(e) {
		e.gf(a)
	});
	a.append(jr)
};
function Yq(a) {
	return Math.max(0, (a.Bd() - 1) * a.j.rk)
}
var lr = '" style="padding-', mr = "right:", nr = "left:", or = 'px">', pr = "</div>";
l.yh = function() {
	var a = new M;
	a.append(fr, this.Fd(), lr, zl(this) ? mr : nr, Yq(this), or, this.Ce(),
			qr(this), rr(this), pr);
	return a.toString()
};
l.Fd = function() {
	return this.j.Aj + (this.dg() ? I + this.j.um : A)
};
var sr = '<span class="', tr = ' title="', ur = "</span>", vr = "<span>";
function rr(a) {
	var b = a.sp, c = new M;
	c.append(sr, a.j.sm, La, b ? tr + Na(b) + La : A, Ja, a.pk, ur, vr, a.Wl,
			ur);
	return c.toString()
}
var wr = '<img class="', xr = '" src="', yr = '<img style="display:none"';
function qr(a) {
	var b = a.Lf();
	return b ? $a(wr, b, xr, a.j.sf, wm) : $a(yr, xr, a.j.sf, wm)
}
var zr = '<img type="expand" class="';
l.Ce = function() {
	return $a(zr, Ar(this), xr, this.j.sf + wm)
};
function Ar(a) {
	var b = a.xa(), c = !b.Ni || b == a.X() && !b.Oi, e = a.j, f = new M;
	f.append(e.od, I, e.km, I);
	if (Al(a)) {
		var g = 0;
		if (b.jp && a.Ie)
			g = a.wa() ? 2 : 1;
		c || (g += !a.gb ? 4 : 8);
		switch (g) {
		case 1:
			f.append(e.om);
			break;
		case 2:
			f.append(e.nm);
			break;
		case 4:
			f.append(e.uj);
			break;
		case 5:
			f.append(e.mm);
			break;
		case 6:
			f.append(e.lm);
			break;
		case 8:
			f.append(e.vj);
			break;
		case 9:
			f.append(e.qm);
			break;
		case 10:
			f.append(e.pm);
			break;
		default:
			f.append(e.tj)
		}
	} else if (c)
		f.append(e.tj);
	else
		!a.gb ? f.append(e.uj) : f.append(e.vj);
	return f.toString()
}
var Br = "background-position:", Cr = ";";
function kr(a) {
	return $a(Br, Dr(a), Cr)
}
var Er = "-100", Fr = "px 0";
function Dr(a) {
	return (!a.gb ? Er : (a.Bd() - 1) * a.j.rk) + Fr
}
l.b = function() {
	var a = Jq.a.b.call(this);
	if (!a)
		this.m = a = this.g().b(this.k());
	return a
};
function Xq(a) {
	return (a = a.b()) ? a.firstChild : i
}
l.sh = function() {
	var a = Xq(this);
	return a ? a.firstChild : i
};
l.Pf = function() {
	var a = Xq(this);
	return a ? a.childNodes[1] : i
};
function Tq(a) {
	return (a = Xq(a)) && a.lastChild ? a.lastChild.previousSibling : i
}
function Uq(a) {
	return (a = a.b()) ? a.lastChild : i
}
var Gr = "document", Hr = "<pre>x", Ir = "</pre>";
function Jr(a) {
	a = a.pk;
	if (a.indexOf(Fa) != -1) {
		if (Gr in n && a.indexOf(Ha) == -1) {
			a = a;
			var b = n.document.createElement(L);
			b.innerHTML = Hr + a + Ir;
			b.firstChild.normalize && b.firstChild.normalize();
			a = b.firstChild.firstChild.nodeValue.slice(1);
			b.innerHTML = A;
			a = a.replace(/(\r\n|\r|\n)/g, Gd)
		} else
			a = Za(a);
		a = a
	} else
		a = a;
	return a
}
function ar(a) {
	var b = Xq(a);
	if (b)
		b.className = a.Fd()
}
l.ee = function() {
	var a = this.sh();
	if (a)
		a.className = Ar(this);
	if (a = Uq(this))
		a.style.backgroundPosition = Dr(this)
};
l.Qe = function(a) {
	if (a.target.getAttribute(id) == dr && Al(this))
		this.Ie && this.Sa(!this.wa());
	else {
		this.select();
		ar(this)
	}
};
l.yo = ve;
l.De = function() {
	if (!this.wa() || !Al(this))
		return this;
	return Z(this, yl(this) - 1).De()
};
l.gk = function() {
	if (Al(this) && this.wa())
		return Z(this, 0);
	else {
		for ( var a = this, b; a != this.xa();) {
			b = a.gb;
			if (b != i)
				return b;
			a = a.X()
		}
		return i
	}
};
l.hk = function() {
	var a = this.uc;
	if (a != i)
		return a.De();
	a = this.X();
	var b = this.xa();
	if (!b.Eb && a == b)
		return i;
	return a
};
function Vq(a, b) {
	if (a.Bc != b) {
		a.Bc = b;
		b.ql(a);
		xl(a, function(c) {
			Vq(c, b)
		})
	}
};
function Kr(a, b, c) {
	Jq.call(this, a, b, c)
}
z(Kr, Jq);
Kr.prototype.Bc = i;
Kr.prototype.xa = function() {
	if (this.Bc)
		return this.Bc;
	var a = this.X();
	if (a)
		if (a = a.xa()) {
			Vq(this, a);
			return a
		}
	return i
};
Kr.prototype.Lf = function() {
	var a = this.wa();
	if (a && this.fh)
		return this.fh;
	if (!a && this.Mh)
		return this.Mh;
	var b = this.j;
	if (Al(this))
		if (a && b.wj)
			return b.od + I + b.wj;
		else {
			if (!a && b.rj)
				return b.od + I + b.rj
		}
	else if (b.yj)
		return b.od + I + b.yj;
	return A
};
var Lr = ".de";
function Mr(a) {
	var b = a.ek();
	if (b)
		b.id = a.k() + Lr
}
function Nr(a) {
	if (a.b()) {
		a = Od(a.g(), i, a.j.Aj, a.b());
		return a.length ? a[0] : i
	}
	return i
}
var Or = "dlrc", Pr = "dldp";
function Qr(a, b) {
	if (a.b()) {
		var c = a.b();
		c.setAttribute(Or, (!/^[\s\xa0]*$/.test(b)).toString());
		c.setAttribute(Pr, b)
	}
};
function Rr(a, b, c, e, f, g) {
	Jq.call(this, a, b, g);
	this.Bb = !!c;
	this.ne = e || i;
	this.Pn = this.j.sq || 0;
	this.Nb = new Iq(this.Zg, 10, this)
}
z(Rr, Kr);
l = Rr.prototype;
l.aa = h;
l.qe = i;
l.d = function() {
	Rr.a.d.call(this);
	this.A(this.aa);
	!this.Bb && this.qe && Qr(this, this.qe);
	return this.b()
};
var Sr = "outerHTML";
function Tr(a) {
	var b = document.createElement(L);
	J(b, go, a.j.im);
	if (Sr in b)
		b = b.outerHTML;
	else {
		a = Vc(b).createElement(L);
		a.appendChild(b.cloneNode(h));
		b = a.innerHTML
	}
	return Ba(b)
}
function Ur(a) {
	return (a = Tq(a)) && a.previousSibling ? a.previousSibling : i
}
l.yh = function() {
	this.xa();
	var a = new M;
	a.append(fr, this.Fd(), lr, zl(this) ? mr : nr, this.Pn + Yq(this), or,
			this.Ce(), qr(this), Tr(this), rr(this), pr);
	return a.toString()
};
l.Ce = function() {
	return this.Ie ? Rr.a.Ce.call(this) : A
};
var Vr = "dlcmt", Wr = "n";
l.B = function() {
	Rr.a.B.call(this);
	Mr(this);
	this.ne && this.ne.Za(Ur(this));
	if (this.Bb)
		if (this.b()) {
			var a = this.b();
			a.setAttribute(Or, h);
			a.setAttribute(Vr, Wr)
		}
	if (!this.aa)
		if (a = this.b())
			a.style.display = Dg
};
l.P = function() {
	Rr.a.P.call(this);
	var a = this.ne;
	if (this.ne) {
		a.P();
		a.b() && yd(a.b())
	}
};
var Xr = "e", Yr = "f";
l.Sa = function(a) {
	Rr.a.Sa.call(this, a);
	this.dispatchEvent(a ? Xr : Yr);
	var b = this.xa();
	if (b)
		b.dispatchEvent(a ? Xr : Yr)
};
l.A = function(a) {
	this.aa = a;
	var b = this.b();
	if (b)
		b.style.display = a ? A : Dg
};
l.V = function() {
	return Og(this.b())
};
l.Qe = function(a) {
	Hq(a) || Rr.a.Qe.call(this, a)
};
l.ek = function() {
	return Nr(this)
};
l.add = function(a, b) {
	var c = Rr.a.add.call(this, a, b);
	this.Nb.start();
	return c
};
l.remove = function(a) {
	this.removeChild(a);
	this.Nb.start()
};
l.Zg = function() {
	this.dispatchEvent(bb)
};
l.select = function() {
	this.j.ao && Rr.a.select.call(this)
};
l.De = function() {
	if ((!this.wa() || !Al(this)) && Zr(this))
		return this;
	for ( var a = Zq(this), b = a.length - 1; b >= 0; b--)
		if (Zr(a[b]))
			return a[b].De();
	return Zr(this) ? this : i
};
l.gk = function() {
	if (Al(this) && this.wa())
		for ( var a = Zq(this), b = 0; b < a.length; b++)
			if (Zr(a[b]))
				return a[b];
	for (a = this; a != this.xa();) {
		b = a.gb;
		if (b != i && Zr(b))
			return b;
		a = a.X()
	}
	return i
};
l.hk = function() {
	var a = this.uc;
	if (a != i && Zr(a))
		return a.De();
	a = this.X();
	var b = this.xa();
	if (!Zr(a) || !b.Eb && a == b)
		return i;
	return a
};
function Zr(a) {
	return a.V() && !a.Bb
}
l.h = function() {
	Rr.a.h.call(this);
	this.Nb.u();
	this.ne = this.Nb = i
};
function $r(a, b, c, e, f, g, k, m) {
	Rr.call(this, a, c, f, undefined, g);
	this.ke = b;
	this.xp = m || A;
	this.rd = e;
	this.fp = k;
	this.Gf = new Pb;
	this.pb = new Op(new Qp)
}
z($r, Rr);
l = $r.prototype;
l.vi = j;
l.Cj = 0;
l.$i = j;
l.al = i;
l.Df = j;
var as = "vr";
l.Sa = function(a) {
	$r.a.Sa.call(this, a);
	a && !this.vi && Dq((this.ke.Hl || A) + as, bs(this, j), this, this.kk)
};
l.Ki = function(a) {
	this.pb.Ki(a)
};
var cs = "&mine=2", ds = '<span class="treedoclistview-node-message">No folders.</span>';
l.kk = function(a) {
	try {
		if (a.readyState == 4 && a.status == 200) {
			var b = Fq(a);
			for (this.Df = h; yl(this) > 0 && !this.vi;)
				this.remove(Z(this, 0));
			this.vi = h;
			var c = b.response;
			this.al = c.resultsCount;
			var e = c ? c.docs || [] : [];
			for (a = 0; a < e.length; a++) {
				var f = e[a], g = f.id;
				if (!this.Gf.contains(g)) {
					es(this, f.name, this.ke, this.j, f, j, this.g(), this.fp,
							cs);
					this.Gf.add(g)
				}
			}
			yl(this) == 0
					&& this.add(new Rr(ds, this.j, h, undefined, this.g()));
			this.Ed();
			this.$i = this.Df = j
		}
	} catch (k) {
	}
};
var fs = '<span class="treedoclistview-node-message">Loading items...</span>';
function es(a, b, c, e, f, g, k, m, o) {
	b = new $r(gs(b, !g, c.jd, f), c, e, f, g, k, m, o);
	a.add(b);
	b.rd && m && b.rd.id == m && b.select();
	a = new Rr(fs, e, h, undefined, k);
	b.add(a);
	b.Ki(h);
	return b
}
var hs = '<img src="', is = '" class="', js = "du-folder-icon", ks = ' style="background-image: url(', ls = '); background-position: 0px"', ms = "/>", ns = "&nbsp;", os = "treedoclistview-node-name", ps = " &nbsp;</span>";
function gs(a, b, c, e) {
	e = e ? e.iconUrl : i;
	return $a(b ? $a(hs, c, is, js, La, e ? ks + e + ls : A, ms, ns) : A, sr,
			os, wm, Na(a), ps)
}
l.Ed = function() {
	Mb(Zq(this), function(a) {
		!a.Bb && a.wa() && a.Ed()
	});
	if (Pp(this.pb, yl(this), this.al, this.b()))
		if (!this.$i) {
			this.$i = h;
			this.Cj += 50;
			Dq((this.ke.Hl || A) + as, bs(this, h), this, this.kk)
		}
};
l.bf = function(a) {
	this.pb.bf(a);
	Mb(Zq(this), function(b) {
		b.Bb || b.bf(a)
	})
};
var qs = "containers=2&desc=false&hidden=1&mimeTypeSets=folder&orphans=2&private=2&published=2&sharedWithDomain=2&sort=1&star=2&view=2&data=1", rs = "&parent=", ss = "&start=", ts = "&numResults=50";
function bs(a, b) {
	var c = qs + (a.rd ? rs + a.rd.id : A) + ss + (b ? a.Cj : 0) + ts + a.xp, e = a.ke;
	if (!e.Vi) {
		var f = us(e, e.Gl), g = e.hg, k = e.rp, m = {
			version : e.Wo
		};
		if (g)
			m.hl = g;
		if (k)
			m.token = k;
		e.Vi = f + us(e, m)
	}
	return c + e.Vi
}
l.h = function() {
	$r.a.h.call(this);
	this.ke = this.rd = i;
	this.Gf.clear();
	this.Gf = i;
	this.pb.u();
	this.pb = i
};
function vs(a) {
	this.ta = {};
	if (a) {
		var b = Lb(a);
		a = Kb(a);
		for ( var c = 0; c < b.length; c++)
			this.t(b[c], a[c])
	}
}
l = vs.prototype;
l.Ba = undefined;
l.t = function(a, b) {
	ws(this, a, b, j)
};
l.add = function(a, b) {
	ws(this, a, b, h)
};
function ws(a, b, c, e) {
	a = a;
	for ( var f = 0; f < b.length; f++) {
		var g = b.charAt(f);
		a.ta[g] || (a.ta[g] = new vs);
		a = a.ta[g]
	}
	if (e && a.Ba !== undefined)
		d(Error('The collection already contains the key "' + b + La));
	else
		a.Ba = c
}
l.w = function(a) {
	for ( var b = this, c = 0; c < a.length; c++) {
		var e = a.charAt(c);
		if (!b.ta[e])
			return;
		b = b.ta[e]
	}
	return b.Ba
};
l.eb = function() {
	var a = [];
	xs(this, a);
	return a
};
function xs(a, b) {
	a.Ba !== undefined && b.push(a.Ba);
	for ( var c in a.ta)
		xs(a.ta[c], b)
}
l.wb = function(a) {
	var b = [];
	if (a) {
		for ( var c = this, e = 0; e < a.length; e++) {
			var f = a.charAt(e);
			if (!c.ta[f])
				return [];
			c = c.ta[f]
		}
		ys(c, a, b)
	} else
		ys(this, A, b);
	return b
};
function ys(a, b, c) {
	a.Ba !== undefined && c.push(b);
	for ( var e in a.ta)
		ys(a.ta[e], b + e, c)
}
l.Oa = function(a) {
	return this.w(a) !== undefined
};
l.clear = function() {
	this.ta = {};
	this.Ba = undefined
};
l.remove = function(a) {
	for ( var b = this, c = [], e = 0; e < a.length; e++) {
		var f = a.charAt(e);
		if (!b.ta[f])
			d(Error('The collection does not have the key "' + a + La));
		c.push([ b, f ]);
		b = b.ta[f]
	}
	a = b.Ba;
	for (delete b.Ba; c.length > 0;) {
		f = c.pop();
		b = f[0];
		f = f[1];
		if (Db(b.ta[f].ta))
			delete b.ta[f];
		else
			break
	}
	return a
};
l.va = function() {
	return new vs(this)
};
l.Rb = function() {
	var a;
	a = this.eb();
	a = typeof a.Rb == s ? a.Rb() : na(a) || u(a) ? a.length : zb(a);
	return a
};
l.rb = function() {
	var a;
	if (a = this.Ba === undefined) {
		a = this.ta;
		a = typeof a.rb == s ? a.rb() : na(a) || u(a) ? a.length == 0 : Db(a)
	}
	return a
};
function zs() {
	this.Sc = new vs
}
l = zs.prototype;
l.ba = A;
l.li = i;
l.jg = i;
l.Me = 0;
l.Jd = 0;
function As(a, b) {
	var c = j, e = a.Sc.wb(b);
	if (e && e.length) {
		a.Jd = 0;
		a.Me = 0;
		if (c = Bs(a, a.Sc.w(e[0])))
			a.li = e
	}
	return c
}
function Bs(a, b) {
	var c;
	if (b) {
		if (a.Jd < b.length) {
			c = b[a.Jd];
			a.jg = b
		}
		if (c) {
			c.zi();
			c.select()
		}
	}
	return !!c
}
l.clear = function() {
	this.ba = A
};
var Cs = "BackgroundImageCache";
function Ds(a, b, c) {
	Jq.call(this, a, b, c);
	this.ye = h;
	$q(this, h);
	this.ga = this;
	this.hf = new zs;
	if (E)
		try {
			document.execCommand(Cs, j, h)
		} catch (e) {
		}
}
z(Ds, Jq);
l = Ds.prototype;
l.La = i;
l.ic = i;
l.mh = j;
l.Mm = i;
l.Ni = h;
l.jp = h;
l.Eb = h;
l.Oi = h;
l.xa = function() {
	return this
};
l.Bd = function() {
	return 0
};
l.zi = function() {
};
var Es = "focused";
l.gn = function() {
	this.mh = h;
	J(this.b(), Es);
	this.ga && this.ga.select()
};
l.Sm = function() {
	this.mh = j;
	Tc(this.b(), Es)
};
l.hasFocus = function() {
	return this.mh
};
l.wa = function() {
	return !this.Eb || Ds.a.wa.call(this)
};
l.Sa = function(a) {
	if (this.Eb)
		Ds.a.Sa.call(this, a);
	else
		this.ye = a
};
l.Ce = function() {
	return A
};
l.Pf = function() {
	var a = Xq(this);
	return a ? a.firstChild : i
};
l.sh = function() {
	return i
};
l.ee = function() {
};
l.Fd = function() {
	return Ds.a.Fd.call(this) + (this.Eb ? A : I + this.j.rm)
};
l.Lf = function() {
	var a = this.wa();
	if (a && this.fh)
		return this.fh;
	if (!a && this.Mh)
		return this.Mh;
	var b = this.j;
	if (a && b.xj)
		return b.od + I + b.xj;
	else if (!a && b.sj)
		return b.od + I + b.sj;
	return A
};
var Fs = "change";
l.ab = function(a) {
	if (this.ga != a) {
		var b = j;
		if (this.ga) {
			b = this.ga == this.Mm;
			$q(this.ga, j)
		}
		if (this.ga = a) {
			$q(a, h);
			b && a.select()
		}
		this.dispatchEvent(Fs)
	}
};
l.ob = function() {
	return this.ga
};
var Gs = "tree";
l.bg = function() {
	Ds.a.bg.call(this);
	var a = this.b();
	Fk(a, Gs);
	X(a, Ml, Tq(this).id)
};
var Hs = "hideFocus";
l.B = function() {
	Ds.a.B.call(this);
	var a = this.b();
	a.className = this.j.tm;
	a.setAttribute(Hs, ac);
	this.Mg();
	this.bg()
};
l.P = function() {
	Ds.a.P.call(this);
	this.Bf()
};
l.Mg = function() {
	var a = this.b();
	a.tabIndex = 0;
	var b = this.La = new qn(a), c = this.ic = new Lk(a);
	this.ja().e(c, Hk, this.Sm).e(c, Jk, this.gn).e(b, yn, this.fb).e(a, Mk,
			this.Eh).e(a, Be, this.Eh).e(a, Gn, this.Eh)
};
l.Bf = function() {
	this.La.u();
	this.La = i;
	this.ic.u();
	this.ic = i
};
l.Eh = function(a) {
	var b;
	a: {
		b = i;
		for ( var c = a.target; c != i;) {
			if (b = Lq[c.id]) {
				b = b;
				break a
			}
			if (c == this.b())
				break;
			c = c.parentNode
		}
		b = i
	}
	if (b)
		switch (a.type) {
		case Mk:
			b.Qe(a);
			break;
		case Be:
			b.yo(a);
			break;
		case Gn:
			b = b;
			a.target.getAttribute(id) == dr && Al(b) || b.Ie && b.Sa(!b.wa())
		}
};
var Is = "~", Js = "\u0080", Ks = "\ufffd";
l.fb = function(a) {
	var b = j;
	b = this.hf;
	var c = j;
	switch (a.keyCode) {
	case 40:
	case 38:
		if (a.ctrlKey) {
			c = a.keyCode == 40 ? 1 : -1;
			var e = b.li;
			if (e) {
				var f = i, g = j;
				if (b.jg) {
					var k = b.Jd + c;
					if (k >= 0 && k < b.jg.length) {
						b.Jd = k;
						f = b.jg
					} else
						g = h
				}
				if (!f) {
					k = b.Me + c;
					if (k >= 0 && k < e.length)
						b.Me = k;
					if (e.length > b.Me)
						f = b.Sc.w(e[b.Me]);
					if (f && f.length && g)
						b.Jd = c == -1 ? f.length - 1 : 0
				}
				if (Bs(b, f))
					b.li = e
			}
			c = h
		}
		break;
	case 8:
		e = b.ba.length - 1;
		c = h;
		if (e > 0) {
			b.ba = b.ba.substring(0, e);
			As(b, b.ba)
		} else if (e == 0)
			b.ba = A;
		else
			c = j;
		break;
	case 27:
		b.ba = A;
		c = h
	}
	if (!(b = c)) {
		if (b = this.ga) {
			b = this.ga;
			c = h;
			switch (a.keyCode) {
			case 39:
				if (a.altKey)
					break;
				if (Al(b))
					b.wa() ? Z(b, 0).select() : b.Sa(h);
				break;
			case 37:
				if (a.altKey)
					break;
				if (Al(b) && b.wa() && b.Ie)
					b.Sa(j);
				else {
					e = b.X();
					f = b.xa();
					if (e && (f.Eb || e != f))
						e.select()
				}
				break;
			case 40:
				(e = b.gk()) && e.select();
				break;
			case 38:
				(e = b.hk()) && e.select();
				break;
			default:
				c = j
			}
			if (c) {
				a.preventDefault();
				(f = b.xa()) && f.hf.clear()
			}
			b = c
		}
		if (!(b = b)) {
			b = this.hf;
			c = j;
			if (!a.ctrlKey && !a.altKey) {
				e = String.fromCharCode(a.charCode || a.keyCode).toLowerCase();
				if ((e.length == 1 && e >= I && e <= Is || e >= Js && e <= Ks)
						&& (e != I || b.ba)) {
					b.ba += e;
					c = As(b, b.ba)
				}
			}
			b = c
		}
		b = b
	}
	(b = b) && a.preventDefault();
	return b
};
l.ql = function(a) {
	var b = this.hf, c = Jr(a);
	if (c && !Aa(c)) {
		c = c.toLowerCase();
		var e = b.Sc.w(c);
		e ? e.push(a) : b.Sc.t(c, [ a ])
	}
};
l.removeNode = function(a) {
	var b = this.hf, c = Jr(a);
	if (c && !Aa(c)) {
		c = c.toLowerCase();
		var e = b.Sc.w(c);
		if (e) {
			mb(e, a);
			e.length && b.Sc.remove(c)
		}
	}
};
var Ls = "goog-tree-expanded-folder-icon", Ms = "goog-tree-collapsed-folder-icon", Kq = {
	sf : "images/cleardot.gif",
	rk : 19,
	tm : "goog-tree-root goog-tree-item",
	rm : "goog-tree-hide-root",
	zj : "goog-tree-item",
	pj : "goog-tree-children",
	qj : "goog-tree-children-nolines",
	Aj : "goog-tree-row",
	sm : "goog-tree-item-label",
	od : "goog-tree-icon",
	km : "goog-tree-expand-icon",
	om : "goog-tree-expand-icon-plus",
	nm : "goog-tree-expand-icon-minus",
	qm : "goog-tree-expand-icon-tplus",
	pm : "goog-tree-expand-icon-tminus",
	mm : "goog-tree-expand-icon-lplus",
	lm : "goog-tree-expand-icon-lminus",
	vj : "goog-tree-expand-icon-t",
	uj : "goog-tree-expand-icon-l",
	tj : "goog-tree-expand-icon-blank",
	wj : Ls,
	rj : Ms,
	yj : "goog-tree-file-icon",
	xj : Ls,
	sj : Ms,
	um : $m
};
function Ns(a, b, c, e) {
	Ds.call(this, a, b, e);
	this.Bb = !!c;
	this.ab(i);
	this.Nb = new Iq(this.Zg, 10, this)
}
z(Ns, Ds);
l = Ns.prototype;
l.qe = i;
l.d = function() {
	Ns.a.d.call(this);
	this.qe && Qr(this, this.qe);
	return this.b()
};
l.gf = function(a) {
	var b = this.xa();
	b = !b.Ni || b == this.X() && !b.Oi ? this.j.qj : this.j.pj;
	var c = this.wa() && Al(this);
	this.Eb && a.append(fr, this.j.zj, gr, this.na, wm, this.yh());
	a.append(fr, b, hr, kr(this), c ? A : ir, wm);
	c && xl(this, function(e) {
		e.gf(a)
	});
	a.append(pr);
	this.Eb && a.append(pr)
};
l.B = function() {
	Ns.a.B.call(this);
	Mr(this)
};
var Os = "goog-tree-checkbox";
function Ps(a) {
	var b = Kq;
	b.sf = a;
	b.im = Os;
	b.ao = h;
	return b
}
l.Sa = function(a) {
	a && Ns.a.Sa.call(this, h)
};
l.Qe = function(a) {
	Hq(a) || Ns.a.Qe.call(this, a)
};
var Qs = "j";
l.select = function() {
	if (!this.Bb) {
		Ns.a.select.call(this);
		this.dispatchEvent(new N(Qs, this))
	}
};
var Rs = "i";
l.ab = function(a) {
	var b = this.ob();
	if (!(a && a.Bb)) {
		Ns.a.ab.call(this, a);
		var c = a && a.Bb, e = b && b.Bb;
		if (b && b != a && b == this)
			this.dispatchEvent(new N(Rs, this));
		else if (!e && c)
			this.dispatchEvent(new N(Qs, this));
		else
			e && !c && this.dispatchEvent(new N(Rs, this))
	}
};
l.ek = function() {
	return Nr(this)
};
l.add = function(a, b) {
	var c = Ns.a.add.call(this, a, b);
	this.Nb.start();
	return c
};
l.remove = function(a) {
	this.removeChild(a);
	this.Nb.start()
};
l.Zg = function() {
	this.dispatchEvent(bb)
};
l.h = function() {
	Ns.a.h.call(this);
	this.Nb.u();
	this.Nb = i
};
var Ss = "Places", Ts = "No collection", Us = "No folder", Vs = "My Collections", Ws = "My Folders", Xs = "Collections shared with me", Ys = "Folders shared with me", Zs = "&mine=1&explicitlyShared=0&advancedSearch=true";
function $s(a, b, c) {
	Y.call(this, b);
	b = new Ns(Ss, Ps(a.jd), h, b);
	if (b.Eb != j) {
		b.Eb = j;
		if (b.C) {
			var e = Xq(b);
			if (e)
				e.className = b.Fd()
		}
		b.ob() == b && Z(b, 0) && b.ab(Z(b, 0))
	}
	this.Dc(b);
	this.ae = b;
	this.mg = new Rr(gs(a.nh ? Ts : Us, j, a.jd, undefined));
	this.ae.add(this.mg);
	this.mg.select();
	e = a.ap;
	this.Oe = es(b, a.nh ? Vs : Ws, a, Ps(a.jd), e, h, this.g(), c);
	this.xg = es(b, a.nh ? Xs : Ys, a, Ps(a.jd), undefined, h, this.g(), c, Zs);
	this.wf = new Ck(this);
	this.pb = new Op(new at(this))
}
z($s, xq);
l = $s.prototype;
l.cm = "du-folders-popup-buttons";
l.jm = "du-folders-popup";
l.Ra = function() {
};
l.d = function() {
	$s.a.d.call(this);
	this.A(j);
	J(this.b(), this.jm);
	var a = this.ae;
	a.d();
	this.g().appendChild(this.Q(), a.b());
	this.ca = this.g().d(L, this.cm);
	this.b().appendChild(this.ca);
	this.Gi(hm)
};
l.B = function() {
	$s.a.B.call(this);
	this.wf.e(this.ca, Be, this.Tm);
	this.wf.e(this.Q().firstChild, Tk, this.Bn)
};
l.Gi = function(a) {
	this.sa = a;
	if (this.ca) {
		a = this.sa;
		a.m = this.ca;
		a.Za()
	}
};
var bt = "b";
l.Tm = function(a) {
	switch (a.target.name) {
	case km:
		var b = this.ae.ob();
		if (b && !this.ae.dg()) {
			this.dispatchEvent(new ct(bt, this.mg.dg() ? i : b.rd));
			this.dispatchEvent(pe);
			this.A(j)
		} else
			a.preventDefault();
		break;
	default:
		this.dispatchEvent(pe);
		this.A(j)
	}
};
l.V = function() {
	return Og(this.b())
};
l.A = function(a) {
	if (a && !this.Bp) {
		this.Bp = h;
		this.Oe.Sa(h);
		var b;
		var c = this.ae.b();
		if (c) {
			b = wg(c);
			c = Gg(c);
			b = new ag(b.x, b.y, c.width, c.height)
		} else
			b = i;
		this.Oe.bf(b);
		this.xg.bf(b)
	}
	U(this.b(), a)
};
l.Bn = function(a, b) {
	this.pb.lk(a, b)
};
l.Ed = function() {
	this.Oe.Ed();
	this.xg.Ed()
};
l.h = function() {
	$s.a.h.call(this);
	this.mg = this.xg = this.Oe = this.ae = i;
	this.wf.u();
	this.wf = i;
	this.pb.u();
	this.Rp = this.sa = this.pb = i
};
function ct(a, b, c) {
	N.call(this, a, c);
	this.we = b
}
z(ct, N);
ct.prototype.h = function() {
	ct.a.call.u(this);
	this.we = i;
	this.pb.u();
	this.pb = i
};
function at(a) {
	this.eg = a
}
z(at, Qp);
at.prototype.getData = function() {
	this.eg.Ed()
};
at.prototype.Pa = function() {
	return h
};
at.prototype.h = function() {
	this.eg = i
};
function dt(a, b) {
	for ( var c = a.db, e = 0; e < c.length; e++) {
		var f = c[e];
		f instanceof cj ? dt(f, b) : b.push(f)
	}
}
function et(a, b) {
	if (b.readyState == 4)
		switch (b.status) {
		case 200:
			var c = Fq(b);
			c.response && c.response.resultFolders
					&& a(c.response.resultFolders)
		}
};
function ft(a) {
	this.oc = [];
	gt(this, a)
}
z(ft, Q);
l = ft.prototype;
l.ga = i;
l.kl = i;
l.th = function() {
	return this.oc.length
};
l.Dd = function(a) {
	return this.oc[a] || i
};
function gt(a, b) {
	if (b) {
		C(b, function(c) {
			ht(this, c, j)
		}, a);
		qb(a.oc, b)
	}
}
l.ac = function(a) {
	this.fd(a, this.th())
};
l.fd = function(a, b) {
	if (a) {
		ht(this, a, j);
		rb(this.oc, b, 0, a)
	}
};
l.removeItem = function(a) {
	if (a && mb(this.oc, a))
		if (a == this.ga) {
			this.ga = i;
			this.dispatchEvent(kl)
		}
};
l.Qd = function(a) {
	this.removeItem(this.Dd(a))
};
l.ob = function() {
	return this.ga
};
l.ab = function(a) {
	if (a != this.ga) {
		ht(this, this.ga, j);
		this.ga = a;
		ht(this, a, h)
	}
	this.dispatchEvent(kl)
};
l.zh = function() {
	return this.ga ? eb(this.oc, this.ga) : -1
};
l.wg = function(a) {
	this.ab(this.Dd(a))
};
l.clear = function() {
	var a = this.oc;
	if (!t(a))
		for ( var b = a.length - 1; b >= 0; b--)
			delete a[b];
	a.length = 0;
	this.ga = i
};
l.h = function() {
	ft.a.h.call(this);
	delete this.oc;
	this.ga = i
};
function ht(a, b, c) {
	if (b)
		if (typeof a.kl == s)
			a.kl(b, c);
		else
			typeof b.Li == s && b.Li(c)
};
function it(a, b, c, e) {
	tp.call(this, a, b, c, e);
	this.zf = a;
	jt(this)
}
z(it, tp);
l = it.prototype;
l.L = i;
l.zf = i;
l.B = function() {
	it.a.B.call(this);
	jt(this);
	kt(this)
};
l.Ua = function(a) {
	it.a.Ua.call(this, a);
	if (a = this.zd()) {
		this.zf = a;
		jt(this)
	} else
		this.wg(0)
};
l.h = function() {
	it.a.h.call(this);
	if (this.L) {
		this.L.u();
		this.L = i
	}
	this.zf = i
};
l.Dh = function(a) {
	this.ab(a.target);
	it.a.Dh.call(this, a);
	a.stopPropagation();
	this.dispatchEvent(Qn)
};
l.En = function() {
	var a = this.ob();
	it.a.Ma.call(this, a && a.M());
	jt(this)
};
l.$e = function(a) {
	var b = it.a.$e.call(this, a);
	if (a != b) {
		this.L && this.L.clear();
		if (a)
			this.L ? xl(a, function(c) {
				this.L.ac(c)
			}, this) : lt(this, a)
	}
	return b
};
l.ac = function(a) {
	it.a.ac.call(this, a);
	this.L ? this.L.ac(a) : lt(this, vp(this))
};
l.fd = function(a, b) {
	it.a.fd.call(this, a, b);
	this.L ? this.L.fd(a, b) : lt(this, vp(this))
};
l.removeItem = function(a) {
	it.a.removeItem.call(this, a);
	this.L && this.L.removeItem(a)
};
l.Qd = function(a) {
	it.a.Qd.call(this, a);
	this.L && this.L.Qd(a)
};
l.ab = function(a) {
	this.L && this.L.ab(a)
};
l.wg = function(a) {
	this.L && this.ab(this.L.Dd(a))
};
l.Ma = function(a) {
	if (a != i && this.L)
		for ( var b = 0, c; c = this.L.Dd(b); b++)
			if (c && typeof c.M == s && c.M() == a) {
				this.ab(c);
				return
			}
	this.ab(i)
};
l.ob = function() {
	return this.L ? this.L.ob() : i
};
l.zh = function() {
	return this.L ? this.L.zh() : -1
};
function lt(a, b) {
	a.L = new ft;
	b && xl(b, function(c) {
		this.L.ac(c)
	}, a);
	kt(a)
}
function kt(a) {
	a.L && a.ja().e(a.L, kl, a.En)
}
function jt(a) {
	var b = a.ob();
	a.$a(b ? b.zd() : a.zf)
}
l.F = function(a) {
	it.a.F.call(this, a);
	this.l & 64 && Lo(vp(this), this.zh())
};
zn("goog-select", function() {
	return new it(i)
});
var mt = "   ", nt = "apps-share-sprite", ot = "  ", pt = "share-pill-private-icon", qt = "unlisted", rt = "share-pill-domain-unlisted-icon", st = "share-pill-unlisted-icon", tt = "public", ut = "share-pill-domain-public-icon", vt = "share-pill-public-icon", wt = '" vhsh="true" vsjson="', xt = '">&nbsp;</span>';
function yt(a, b) {
	var c = b || new M;
	c.append(sr, go, mt, nt, ot);
	switch (a.state) {
	case Zp:
		c.append(pt);
		break;
	case qt:
		c.append(a.tg ? rt : st);
		break;
	case tt:
		c.append(a.tg ? ut : vt)
	}
	c.append(wt, Wd(a.Cp), xt);
	if (!b)
		return c.toString()
}
var zt = "People at ", At = " with the link", Bt = "Private", Ct = "Public on the web", Dt = "Anyone with the link";
function Et(a, b) {
	var c = b || new M;
	if (a.tg && a.state != Zp)
		if (a.state == tt) {
			var e = Wd(a.pg);
			c.append(e)
		} else {
			e = zt + (Wd(a.pg) + At);
			c.append(e)
		}
	else
		switch (a.state) {
		case Zp:
			c.append(Bt);
			break;
		case tt:
			c.append(Ct);
			break;
		case qt:
			c.append(Dt)
		}
	if (!b)
		return c.toString()
};
var Ft = '<table class="visibility-select-item"><tbody><tr><td>', Gt = '</td><td class="visibility-select-item-title">', Ht = '</td></tr><tr><td></td><td class="visibility-select-item-body">', It = "Only people explicitly granted permission can access. Sign-in required.", Jt = " can find and access.", Kt = " who have the link can access.", Lt = "Anyone on the Internet can find and access. No sign-in required.", Mt = "Anyone who has the link can access. No sign-in required.", Nt = "</td></tr></tbody></table>";
function Ot(a, b) {
	var c;
	var e = a.Wf();
	c = new M;
	c.append(Ft);
	yt(e, c);
	c.append(Gt);
	Et(e, c);
	c.append(Ht);
	var f = c || new M;
	if (e.state == Zp)
		f.append(It);
	else if (e.tg)
		if (e.state == tt) {
			e = zt + (Wd(e.pg) + Jt);
			f.append(e)
		} else {
			e = zt + (Wd(e.pg) + Kt);
			f.append(e)
		}
	else
		e.state == tt ? f.append(Lt) : f.append(Mt);
	c.append(Nt);
	c = c.toString();
	c = ud(document, c);
	hp.call(this, c, a, b, Pt.O())
}
z(Ot, hp);
var Qt = '<table class="visibility-select-item-caption"><tbody><tr><td>', Rt = '</td><td class="visibility-select-item-caption-summary">', St = '</td><td><span class="goog-inline-block goog-imageless-popup-button-dropdown"></span></td></tr></tbody></table>';
Ot.prototype.zd = function() {
	var a;
	a = this.Rc.Wf();
	var b = new M;
	b.append(Qt);
	yt(a, b);
	b.append(Rt);
	Et(a, b);
	b.append(St);
	a = b.toString();
	return ud(document, a)
};
function Pt() {
	this.Tg = []
}
z(Pt, Wo);
r(Pt);
var Tt = "visibility-select-item";
Pt.prototype.o = function() {
	return Tt
};
function Ut(a, b) {
	it.call(this, undefined, undefined, a, b)
}
z(Ut, it);
function Vt(a, b) {
	for ( var c = vp(a); Al(c);)
		a.Qd(0);
	for (c = 0; c < b.length; c++)
		a.ac(new Ot(b[c]));
	a.wg(0)
};
function Wt(a, b, c) {
	Ff.call(this, a);
	this.Wj = b;
	this.kh = c;
	this.kf = j
}
z(Wt, Ff);
Wt.prototype.abort = function() {
	this.kf && pa(this.kh.cancelUpload) && this.kh.cancelUpload(this.Wj.id);
	this.dispatchEvent(Of)
};
Wt.prototype.yc = function(a) {
	this.kh.startUpload(this.Wj.id, a);
	this.dispatchEvent(pf);
	this.kf = h
};
var Xt, Yt, Zt = "Shockwave Flash", $t = "Shockwave Flash 2.0", au = "2.0.0.11", bu = "application/x-shockwave-flash", cu = "ShockwaveFlash.ShockwaveFlash.7", du = "$version", eu = "ShockwaveFlash.ShockwaveFlash.6", fu = "6.0.21", gu = "ShockwaveFlash.ShockwaveFlash";
(function() {
	function a(m) {
		m = m.match(/[\d]+/g);
		m.length = 3;
		return m.join(ba)
	}
	var b = j, c = A;
	if (navigator.plugins && navigator.plugins.length) {
		var e = navigator.plugins[Zt];
		if (e) {
			b = h;
			if (e.description)
				c = a(e.description)
		}
		if (navigator.plugins[$t]) {
			b = h;
			c = au
		}
	} else if (navigator.mimeTypes && navigator.mimeTypes.length) {
		if (b = (e = navigator.mimeTypes[bu]) && e.enabledPlugin)
			c = a(e.enabledPlugin.description)
	} else
		try {
			e = new ActiveXObject(cu);
			b = h;
			c = a(e.GetVariable(du))
		} catch (f) {
			try {
				e = new ActiveXObject(eu);
				b = h;
				c = fu
			} catch (g) {
				try {
					e = new ActiveXObject(gu);
					b = h;
					c = a(e.GetVariable(du))
				} catch (k) {
				}
			}
		}
	Xt = b;
	Yt = c
})();
var hu = "overlayElement must be defined", iu = "FLASH_UPLOADER_", ju = "uploader-overlay-visible", ku = "upload-uploader-flash-content", lu = "relative", mu = "always", nu = "transparent", ou = "apiId=", pu = "param";
function qu(a) {
	if (!a)
		d(Error(hu));
	Zi.call(this);
	this.Qk = new D;
	this.Pj = new D;
	this.Oj = new D;
	this.If = new D;
	this.rg = new D;
	this.Cg = new D;
	this.ub = j;
	var b = iu + ru++;
	if (!this.qb) {
		this.qb = b;
		md().onUploaderApiReady = su
	}
	tu.t(b, w(this.Lk, this));
	this.Qk.t(b, a);
	a = Wc(a);
	J(a, ju);
	var c = K(a), e = Gg(a), f = c.d(L, {
		"class" : ku
	});
	c.Sh(f, a);
	bg(f, gg, ng);
	bg(a.parentNode, gg, lu);
	jg(f, new lc(a.offsetLeft, a.offsetTop));
	c = uu;
	a = {
		allowscriptaccess : mu,
		wmode : nu,
		flashvars : ou + b
	};
	e = {
		type : bu,
		id : b,
		width : e.width,
		height : e.height
	};
	if (E)
		a.movie = c;
	else
		e.data = c;
	b = Wc(f);
	f = K(b);
	e = f.d(da, e);
	for ( var g in a) {
		c = f.d(pu, {
			name : g,
			value : a[g]
		});
		e.appendChild(c)
	}
	b.appendChild(e)
}
z(qu, Zi);
var vu = "scotty flash";
qu.prototype.Ad = function() {
	return vu
};
qu.prototype.tf = function(a) {
	return new gh(a)
};
var uu = "static/uploaderapi.swf", ru = 1, tu = new D;
l = qu.prototype;
l.qb = A;
l.af = function(a) {
	qu.a.af.call(this, a);
	a = this.pl;
	C(this.If.wb(), a, this)
};
l.pl = function(a) {
	Wc(a || this.qb).setMultiSelect(this.sc)
};
function su(a) {
	tu.w(a)(a)
}
var wu = "flashFileSelection", xu = "onSelected", yu = "flashMouseEvent", zu = "onMouseOver", Au = "onMouseOut", Bu = "onMouseClick", Cu = "flashToFileIo", Du = "onOpen", Eu = "onProgress", Fu = "onCompleteData", Gu = "onHttpError", Hu = "onSecurityError", Iu = "onIoError", Ju = "flashImageEvent", Ku = "onPreviewReady", Lu = "onLoadError";
l.Lk = function(a) {
	var b = Wc(a);
	if (b.addListener) {
		this.If.t(a, h);
		var c = a + wu, e = md();
		e[c] = w(this.Md, this);
		b.addListener(xu, c);
		c = a + yu;
		e[c] = w(this.Co, this);
		b.addListener(zu, c);
		b.addListener(Au, c);
		b.addListener(Bu, c);
		c = a + Cu;
		e[c] = w(this.Km, this);
		b.addListener(Du, c);
		b.addListener(Eu, c);
		b.addListener(Fu, c);
		b.addListener(Gu, c);
		b.addListener(Hu, c);
		b.addListener(Iu, c);
		c = a + Ju;
		e[c] = w(this.Eo, this);
		b.addListener(Ku, c);
		b.addListener(Lu, c);
		this.pl(a);
		Wc(a || this.qb).setAllowedFileTypes(Mu(this));
		this.ol(a)
	} else
		V(this.Lk, undefined, this)
};
l.vc = function(a) {
	this.rg.Oa(a.k()) ? this.Cg.t(a.k(), h) : qu.a.vc.call(this, a)
};
l.Md = function(a) {
	var b = a.apiId;
	this.ub && Dj(this);
	var c = a.files;
	a = gb(c, function(e) {
		return new lf(e.name, e.size)
	});
	this.dispatchEvent(new Zg(zk, a));
	C(a, function(e, f) {
		var g = W(this, e);
		g.Xj = b;
		g.Vj = c[f];
		this.Oj.t(c[f].id, e);
		this.Ec(e)
	}, this)
};
var Nu = "data:image/jpg;base64,", Ou = "filepreviewerror";
l.Eo = function(a) {
	var b = this.Oj.w(a.file);
	this.rg.remove(b.k());
	switch (a.type) {
	case Ku:
		this.dispatchEvent(new dh(b, Nu + a.data, a.version));
		break;
	case Lu:
		this.dispatchEvent(new $g(Ou, b))
	}
	this.Cg.w(b.k(), j) && this.vc(b)
};
var Pu = "HTTP Error ", Qu = "An error occured while reading the file.";
l.Km = function(a) {
	var b = this.Pj.w(a.file);
	switch (a.type) {
	case Eu:
		Kf(b, a.bytesLoaded);
		break;
	case Fu:
		b.Lc(a.data);
		break;
	case Gu:
		b.handleError(rj, Pu + a.code);
		break;
	case Hu:
		b.handleError(xj, a.error);
		break;
	case Iu:
		b.handleError(vj, Qu)
	}
};
var Ru = "flashmouseover", Su = "flashmouseout", Tu = "flashmouseclick";
l.Co = function(a) {
	var b;
	switch (a.type) {
	case zu:
		b = Ru;
		break;
	case Au:
		b = Su;
		break;
	case Bu:
		b = Tu
	}
	this.dispatchEvent(b)
};
l.nd = function(a) {
	var b = W(this, a).Vj, c = W(this, a).Xj;
	a = new Wt(a, b, Wc(c || this.qb));
	this.Pj.t(b.id, a);
	return a
};
var Uu = "Allowed files", Vu = ";*";
function Mu(a) {
	a = a.ih;
	if (t(a) && a.length)
		return [ {
			description : Uu,
			extension : Zc + a.join(Vu)
		} ]
}
l.Hb = function(a) {
	this.ub = a;
	this.If.Oa(this.qb) && this.Ib
			&& Wc(this.qb || this.qb).browse(this.sc, Mu(this))
};
l.Sd = function(a) {
	qu.a.Sd.call(this, a);
	a = this.ol;
	C(this.If.wb(), a, this)
};
var Wu = "uploader-overlay-hidden";
l.ol = function(a) {
	var b = this.Ib;
	a = Wc(a || this.qb);
	a.setAllowAddingFiles(b);
	!b ? J(a, Wu) : Tc(a, Wu)
};
function Xu(a) {
	this.v = a
}
z(Xu, Qd);
l = Xu.prototype;
l.name = "Base";
l.version = 1;
l.Xe = [];
l.Al = function() {
};
l.getName = function() {
	return this.name
};
var Yu = "INSERT OR REPLACE INTO StoreVersionInfo (StoreName, Version) VALUES(?,?)";
function Zu(a, b) {
	a.v.execute(Yu, a.name, b)
}
var $u = "CREATE TRIGGER ", av = " ON ", bv = " WHEN ", cv = " BEGIN ", dv = "; ", ev = "; END";
function fv(a, b, c, e) {
	return $u + e + c.name + I + b + av + c.np + (c.Dp ? bv + c.Dp : A) + cv
			+ c.Lp.join(dv) + ev
}
var gv = "IF NOT EXISTS ", hv = "CREATE TABLE ", iv = " (\n", jv = ",\n  ", kv = "CREATE VIRTUAL TABLE ", lv = " USING FTS2 (\n", mv = "CREATE", nv = " UNIQUE", ov = " INDEX ", pv = "BEFORE INSERT", qv = "AFTER INSERT", rv = "BEFORE UPDATE", sv = "AFTER UPDATE", tv = "BEFORE DELETE", uv = "AFTER DELETE";
function vv(a, b, c) {
	c = c ? gv : A;
	switch (b.type) {
	case 1:
		return hv + c + b.name + iv + b.kd.join(jv) + Zd;
	case 2:
		return kv + c + b.name + lv + b.kd.join(jv) + Zd;
	case 3:
		return mv + (b.vq ? nv : A) + ov + c + b.name + av + b.np + iv
				+ b.kd.join(jv) + Zd;
	case 4:
		return fv(a, pv, b, c);
	case 5:
		return fv(a, qv, b, c);
	case 6:
		return fv(a, rv, b, c);
	case 7:
		return fv(a, sv, b, c);
	case 8:
		return fv(a, tv, b, c);
	case 9:
		return fv(a, uv, b, c)
	}
	return A
}
var wv = "DROP TABLE IF EXISTS ", xv = "DROP INDEX IF EXISTS ", yv = "DROP TRIGGER IF EXISTS ";
function zv(a, b) {
	switch (b.type) {
	case 1:
	case 2:
		return wv + b.name;
	case 3:
		return xv + b.name;
	case 4:
	case 5:
	case 6:
	case 7:
	case 8:
	case 9:
		return yv + b.name
	}
	return A
}
function Av(a, b, c) {
	Bv(a.v, a.v.re);
	try {
		for ( var e = 0; e < b.length; ++e)
			a.v.execute(vv(a, b[e], c));
		Cv(a.v)
	} catch (f) {
		Dv(a.v, f)
	}
}
function Ev(a, b) {
	Bv(a.v, a.v.re);
	try {
		for ( var c = b.length - 1; c >= 0; --c)
			a.v.execute(zv(a, b[c]));
		Cv(a.v)
	} catch (e) {
		Dv(a.v, e)
	}
}
l.h = function() {
	Xu.a.h.call(this);
	this.v = i
};
function Fv(a, b) {
	this.v = a;
	this.Fl = b
}
z(Fv, Xu);
l = Fv.prototype;
l.name = "FileStore";
l.version = 1;
var Gv = "Files";
l.Xe = [
		{
			type : 1,
			name : "UploaderInstances",
			kd : [ "UploaderInstanceId TEXT PRIMARY KEY",
					"SessionServerUrl TEXT NOT NULL" ]
		},
		{
			type : 1,
			name : Gv,
			kd : [
					"FileId INTEGER PRIMARY KEY AUTOINCREMENT",
					"UploaderInstanceId TEXT NOT NULL REFERENCES UploaderInstances(UploaderInstanceId)",
					"Path TEXT NOT NULL", "BytesTotal INTEGER NOT NULL",
					"BytesTransferred INTEGER NOT NULL DEFAULT 0",
					"UploadUrl TEXT NOT NULL",
					"UNIQUE(UploaderInstanceId, Path, BytesTotal) ON CONFLICT REPLACE" ]
		},
		{
			type : 1,
			name : "FileMetadata",
			kd : [ "FileId INTEGER REFERENCES File(Id)", "Key TEXT NOT NULL",
					"Value TEXT NOT NULL", "PRIMARY KEY (FileId, Key)" ]
		} ];
l.Al = function() {
	Ev(this, this.Xe);
	Av(this, this.Xe)
};
var Hv = "INSERT INTO Files(UploaderInstanceId, Path, BytesTotal, UploadUrl) VALUES(?,?,?,?)";
l.Ec = function(a) {
	var b = this.v;
	b.execute(Hv, this.Fl, a.U.fa, a.U.Na, a.U.ie);
	a.Nd = b.v.lastInsertRowId
};
var Iv = "DELETE FROM Files WHERE FileId = ?";
l.Wb = function(a) {
	this.v.execute(Iv, a.Nd);
	a.Nd = i
};
l.Si = function(a) {
	var b = this.v;
	a = xa(C, a, this.up, this);
	Bv(b, b.re);
	try {
		a();
		Cv(b)
	} catch (c) {
		Dv(b, c);
		d(c)
	}
};
var Jv = "UPDATE Files SET BytesTransferred = ? WHERE FileId = ?";
l.up = function(a) {
	if (a.wi) {
		this.v.execute(Jv, a.U.Kb, a.Nd);
		a.wi = j
	}
};
var Kv = "google.gears.factory", Lv = "GearsFactory", Mv = "Gears.Factory", Nv = "ie_mobile";
function Ov() {
	if (Pv != undefined)
		return Pv;
	var a = ca(Kv);
	if (a)
		return Pv = a;
	try {
		return Pv = new (ca(Lv))
	} catch (b) {
	}
	try {
		a = new ActiveXObject(Mv);
		a.getBuildInfo().indexOf(Nv) != -1 && a.privateSetGlobalObject(n);
		return Pv = a
	} catch (c) {
	}
	return Pv = Qv()
}
var Rv = "window", Sv = "application/x-googlegears", Tv = "gears-factory";
function Qv() {
	var a = ca(Rv);
	if (a && a.navigator.mimeTypes[Sv])
		try {
			var b = a.document, c = b.getElementById(Tv);
			if (!c) {
				c = b.createElement(da);
				c.style.display = Dg;
				c.width = Ya;
				c.height = Ya;
				c.type = Sv;
				c.id = Tv;
				b.documentElement.appendChild(c)
			}
			if (typeof c.create != "undefined")
				return c
		} catch (e) {
		}
	return i
}
var Pv = undefined, Uv = "navigator.mimeTypes";
function Vv() {
	if (Wv != undefined)
		return Wv;
	var a = ca(Kv);
	if (a || ca(Lv))
		return Wv = h;
	if (typeof ActiveXObject != "undefined")
		try {
			new ActiveXObject(Mv);
			return Wv = h
		} catch (b) {
			return Wv = j
		}
	if ((a = ca(Uv)) && a[Sv])
		if (a = Qv()) {
			Pv = a;
			return Wv = h
		}
	return Wv = j
}
var Wv = undefined;
function Xv(a, b) {
	a = Number(a);
	b = Number(b);
	this.start = a < b ? a : b;
	this.end = a < b ? b : a
}
Xv.prototype.va = function() {
	return new Xv(this.start, this.end)
};
function Yv(a) {
	Ff.call(this, a);
	this.Yb = this.I = i;
	this.l = nf;
	this.uf = i;
	this.gd = j;
	this.ef = i;
	this.Uk = 0;
	Zv(this, i)
}
z(Yv, Ff);
var $v = "initialquery", aw = "finalquery", bw = "paused", cw = "resuming", dw = {
	bj : nf,
	Hg : qf,
	cj : $v,
	aj : sf,
	Ol : aw,
	Jp : bw,
	Sl : cw
};
l = Yv.prototype;
l.Fi = function(a) {
	this.gd = a
};
function Zv(a, b) {
	if ((b ? b.end - b.start + 1 : xf(a.Da())) > 5242880)
		if (b)
			b.end = b.start + 5242880 - 1;
		else
			b = new Xv(0, 5242879);
	a.Yb = b
}
l.abort = function() {
	ew(this);
	this.l = nf;
	this.dispatchEvent(Of)
};
function ew(a) {
	fw(a);
	if (a.I) {
		a.I.abort();
		a.I = i
	}
}
var gw = "X-HTTP-Method-Override", hw = "PUT", iw = "application/octet-stream", jw = "Content-Range", kw = "bytes ", lw = "*/0";
l.yc = function(a) {
	var b = new D;
	b.t(gw, hw);
	b.t(Ph, iw);
	if (this.Yb) {
		var c = xf(this.Da());
		b.t(jw, kw + (c == 0 ? lw : this.Yb.start + xk + this.Yb.end + R + c))
	}
	this.I = this.oj();
	this.nl(a, b, this.Yb);
	this.l = qf;
	this.dispatchEvent(pf)
};
l.pause = function() {
	if (this.l == nf)
		d(Error("Cannot pause an upload that is not started"));
	ew(this);
	this.l = bw
};
l.cl = function() {
	if (this.l != bw)
		d(Error("Cannot resume an upload that is not paused"));
	this.l = cw;
	mw(this, this.Ei, 1E3)
};
function mw(a, b, c) {
	fw(a);
	a.ef = V(function() {
		this.ef = i;
		b.call(this)
	}, c, a)
}
function fw(a) {
	if (a.ef != i) {
		lh.clearTimeout(a.ef);
		a.ef = i
	}
}
var nw = "bytes */";
l.Ei = function() {
	var a = new Lh;
	this.I = a;
	O(a, Zh, this.Ih, j, this);
	a.send(this.Sf(), Oh, A, {
		"X-HTTP-Method-Override" : hw,
		"Content-Range" : nw + xf(this.Da())
	})
};
var ow = "Unknown request state";
l.Ih = function() {
	switch (this.vh()) {
	case 1:
	case 2:
	case 3:
	case 0:
		break;
	case 4:
		this.Fe();
		break;
	default:
		this.handleError(Vf, ow)
	}
};
var pw = "Range", qw = "Illegal Range header: ", rw = "autoretry";
l.Fe = function() {
	var a = this.wh();
	this.sg = a;
	var b = this.xh();
	switch (this.Vf()) {
	case 308:
		if (b = this.I.getResponseHeader(pw)) {
			(a = /^bytes=(\d+)-(\d+)$/.exec(b)) || this.handleError(Vf, qw + b);
			b = Number(a[1]);
			a = Number(a[2]);
			b > 0 ? Zv(this, new Xv(0, b - 1)) : Zv(this, new Xv(a + 1, xf(this
					.Da()) - 1))
		} else
			Zv(this, i);
		switch (this.l) {
		case aw:
			this.dispatchEvent(rw);
		case qf:
		case cw:
			this.yc(this.Sf());
			break;
		case $v:
			sw(this)
		}
		break;
	case 200:
		this.Lc(a);
		break;
	case 408:
	case 500:
	case 502:
	case 503:
	case 504:
		this.gd ? tw(this) : this.handleError(rj, b);
		break;
	default:
		this.handleError(rj, b)
	}
};
var uw = "Illegal state while handling retryable error";
function tw(a) {
	switch (a.l) {
	case dw.Sl:
	case dw.Hg:
		a.l = dw.cj;
		a.Ei();
		break;
	case dw.cj:
	case dw.Ol:
		sw(a);
		break;
	default:
		a.handleError(Vf, uw)
	}
}
function sw(a) {
	a.l = sf;
	var b = a.Yb ? a.Yb.start : 0;
	if (a.uf == i || b > a.Uk)
		a.uf = 5E3;
	else
		a.uf *= 2;
	a.Uk = b;
	mw(a, a.ll, a.uf);
	a.dispatchEvent(sf)
}
l.ll = function() {
	this.l = aw;
	this.Ei()
};
l.Lc = function(a) {
	Yv.a.Lc.call(this, a);
	this.l = nf
};
l.handleError = function(a, b) {
	Yv.a.handleError.call(this, a, b);
	this.l = nf
};
l.vh = function() {
	return ai(this.I)
};
l.wh = function() {
	return this.I.xb()
};
l.Vf = function() {
	return bi(this.I)
};
l.xh = function() {
	return ci(this.I)
};
function vw(a, b, c) {
	Yv.call(this, a);
	this.Nm = b;
	this.ml = this.yi = c
}
z(vw, Yv);
l = vw.prototype;
l.yc = function(a) {
	if (this.ml) {
		this.ml = j;
		this.ll()
	} else
		vw.a.yc.call(this, a)
};
var ww = "beta.httprequest";
l.oj = function() {
	var a = Ov().create(ww);
	a.onreadystatechange = w(this.Ih, this);
	a.upload.onprogress = w(this.vn, this);
	return a
};
l.nl = function(a, b, c) {
	var e = this.Nm.blob;
	if (c)
		e = e.slice(c.start, c.end - c.start + 1);
	var f = this.I;
	f.open(Oh, a);
	b && Mb(b, function(g, k) {
		f.setRequestHeader(k, g)
	}, this);
	f.send(e)
};
l.vh = function() {
	var a = this.I;
	return a instanceof Lh ? vw.a.vh.call(this) : a.readyState
};
l.wh = function() {
	var a = this.I;
	return a instanceof Lh ? vw.a.wh.call(this) : a.responseText
};
l.Vf = function() {
	var a = this.I;
	return a instanceof Lh ? vw.a.Vf.call(this) : a.status
};
l.xh = function() {
	var a = this.I;
	return a instanceof Lh ? vw.a.xh.call(this) : a.statusText
};
l.vn = function(a) {
	var b = this.Yb;
	Kf(this, (b ? b.start : 0) + a.loaded)
};
l.Fe = function() {
	vw.a.Fe.call(this)
};
var xw = "beta.database", yw = "1.0";
function zw(a, b) {
	var c = Ov();
	try {
		this.v = c.create(xw, yw)
	} catch (e) {
		d(Error("Could not create the database. " + e.message))
	}
	if (this.v != i) {
		c = a + xk + b;
		if (!c)
			d(Error("file name empty"));
		c = String(c);
		var f = c.replace(/[^a-zA-Z0-9\.\-@_]/g, A);
		if (!f)
			d(Error("file name invalid: " + c));
		if (f.length <= 64)
			c = f;
		else {
			for ( var g = 0, k = 0; k < c.length; ++k) {
				g = 31 * g + c.charCodeAt(k);
				g %= 4294967296
			}
			c = f.substring(0, 54) + String(g)
		}
		c = c;
		this.gl = c;
		this.v.open(c)
	} else
		d(Error("Could not create the database"))
}
z(zw, Q);
function Aw(a) {
	N.call(this, a)
}
z(Aw, N);
zw.prototype.Xi = h;
zw.prototype.sb = 0;
zw.prototype.Pe = j;
zw.prototype.re = "IMMEDIATE";
var Bw = {
	DEFERRED : 0,
	IMMEDIATE : 1,
	EXCLUSIVE : 2
};
l = zw.prototype;
l.Bj = Bw.DEFERRED;
function Cw(a) {
	return a && a.isValidRow() ? a.field(0) : i
}
function Dw(a) {
	var b = [];
	if (a && a.isValidRow())
		for ( var c = a.fieldCount(), e = 0; e < c; e++)
			b[e] = a.field(e);
	return b
}
var Ew = ": ";
l.execute = function(a) {
	a = String(a);
	var b;
	try {
		if (arguments.length == 1)
			return this.v.execute(a);
		b = arguments.length == 2 && t(arguments[1]) ? arguments[1] : sb(
				arguments, 1);
		return this.v.execute(a, b)
	} catch (c) {
		if (b)
			a += Ew + be(b);
		d(Tb(c, a))
	}
};
function Fw(a, b, c, e) {
	if (e.length == 0 || 1 >= e.length)
		a = a.execute(b);
	else if (t(e[1]))
		a = a.execute(b, e[1]);
	else {
		e = Array.prototype.slice.call(e, 1);
		a = a.execute(b, e)
	}
	try {
		return c(a)
	} finally {
		a && a.close()
	}
}
l.Yo = function(a) {
	return Fw(this, a, Cw, arguments)
};
l.Xo = function(a) {
	return Fw(this, a, Dw, arguments)
};
var Gw = "ROLLBACK", Hw = "COMMIT", Iw = "beforerollback", Jw = "beforecommit", Kw = "rollback", Lw = "commit";
function Mw(a, b) {
	var c;
	c = b ? Gw : Hw;
	var e = a.dispatchEvent(new Aw(b ? Iw : Jw));
	if (e) {
		a.v.execute(c);
		a.sb = 0;
		a.dispatchEvent(new Aw(b ? Kw : Lw))
	}
	return e
}
var Nw = "beforebegin", Ow = "BEGIN ", Pw = "begin";
function Bv(a, b) {
	if (a.Xi)
		if (a.sb == 0) {
			a.Pe = j;
			a.dispatchEvent(new Aw(Nw));
			a.v.execute(Ow + b);
			a.Bj = Bw[b];
			a.sb = 1;
			try {
				a.dispatchEvent(new Aw(Pw))
			} catch (c) {
				a.v.execute(Gw);
				a.sb = 0;
				d(c)
			}
			return h
		} else if (a.Pe)
			d(Error("Cannot begin a transaction with a rollback pending"));
		else if (Bw[b] > a.Bj)
			d(Error("Cannot elevate the level within a nested begin"));
		else
			a.sb++;
	return j
}
var Qw = "Unbalanced transaction";
function Cv(a) {
	if (a.Xi) {
		if (a.sb <= 0)
			d(Error(Qw));
		if (a.sb == 1) {
			var b = Mw(a, a.Pe);
			return !a.Pe && b
		} else
			a.sb--
	}
	return j
}
function Dv(a, b) {
	var c = h;
	if (a.Xi) {
		if (a.sb <= 0)
			d(Error(Qw));
		if (a.sb == 1)
			c = Mw(a, h);
		else {
			a.sb--;
			a.Pe = h;
			if (b)
				d(b);
			return j
		}
	}
	return c
}
l.open = function() {
	if (this.v && this.gl)
		this.v.open(this.gl);
	else
		d(Error("Could not open the database"))
};
l.close = function() {
	this.v && this.v.close()
};
l.h = function() {
	zw.a.h.call(this);
	this.v = i
};
l.remove = function() {
	this.v.remove()
};
var Rw = "beta.desktop";
function Sw(a) {
	Zi.call(this);
	this.Ej = Ov().create(Rw);
	this.ub = j;
	this.gd = h;
	this.Wk = (this.hc = a || i) && new Np(this.tp, 2E3, this)
}
z(Sw, Zi);
l = Sw.prototype;
l.$h = function() {
	return h
};
var Tw = "scotty gears";
l.Ad = function() {
	return Tw
};
l.Fi = function(a) {
	this.gd = a
};
var Uw = "0.5.30.0";
l.Uh = function() {
	var a = Ov().version;
	return cb(a, Uw) >= 0
};
var Vw = "application/x-gears-files";
l.ej = function(a) {
	var b = this.Ej.getDragData(a.Ja, Vw);
	this.Md(b && b.files);
	a.stopPropagation();
	a.preventDefault()
};
l.tf = function(a) {
	return new hh(a)
};
l.nd = function(a) {
	var b = W(this, a).dk;
	a = new vw(a, b, a.yi);
	a.Fi(this.gd);
	O(a, sf, w(this.Rm, this));
	O(a, rw, w(this.Qm, this));
	return a
};
l.Hb = function(a) {
	if (this.Ib) {
		this.ub = a;
		a = {
			singleFile : !this.sc
		};
		var b = this.ih;
		if (t(b) && b.length)
			a.filter = b;
		this.Ej.openFiles(w(this.Md, this), a)
	}
};
var Ww = "SELECT FileId, BytesTransferred, UploadUrl FROM Files WHERE UploaderInstanceId = ? AND Path = ? AND BytesTotal = ?";
l.nf = function(a) {
	if (this.hc) {
		var b;
		var c = this.hc;
		b = W(this, a);
		c = c.v.Xo(Ww, c.Fl, b.U.fa, b.U.Na);
		if (c.length === 0)
			b = j;
		else {
			b.Nd = c[0];
			var e = c[1];
			if (e)
				b.U.Kb = e;
			b.U.ie = c[2];
			b = b.U.yi = h
		}
		b && this.dispatchEvent(new $g(pj, a))
	} else
		Sw.a.nf.call(this, a)
};
l.si = function(a) {
	Sw.a.si.call(this, a);
	W(this, a.target.Da()).wi = h;
	this.Wk && this.Wk.jh()
};
l.tp = function() {
	this.hc && this.hc.Si(gb(this.gc, function(a) {
		return W(this, a)
	}, this))
};
l.Md = function(a) {
	if (a.length != 0) {
		this.ub && Dj(this);
		var b = gb(a, function(c) {
			return new lf(c.name, c.blob.length)
		});
		this.dispatchEvent(new Zg(zk, b));
		C(b, function(c, e) {
			W(this, c).dk = a[e];
			this.Ec(c)
		}, this)
	}
};
l.Rm = function(a) {
	this.Ga(a.target.Da(), sf)
};
l.Qm = function(a) {
	this.Ga(a.target.Da(), qf)
};
l.Ga = function(a, b) {
	var c = W(this, a).state;
	Sw.a.Ga.call(this, a, b);
	if (this.hc) {
		b !== c && b === qf && this.hc.Ec(W(this, a));
		Ej(c) && !Ej(b) && this.hc.Wb(W(this, a))
	}
};
function Xw(a, b, c, e) {
	if (!t(a) || !t(b))
		d(Error("Start and end parameters must be arrays"));
	if (a.length != b.length)
		d(Error("Start and end points must be the same length"));
	this.cf = a;
	this.Cm = b;
	this.duration = c;
	this.dj = e;
	this.coords = []
}
z(Xw, Q);
var Yw = {}, Zw = i;
function $w() {
	lh.clearTimeout(Zw);
	var a = ya(), b;
	for (b in Yw)
		ax(Yw[b], a);
	Zw = Db(Yw) ? i : lh.setTimeout($w, 20)
}
function bx(a) {
	a = ra(a);
	delete Yw[a];
	if (Zw && Db(Yw)) {
		lh.clearTimeout(Zw);
		Zw = i
	}
}
l = Xw.prototype;
l.l = 0;
l.bk = 0;
l.Xa = 0;
l.startTime = i;
l.Jj = i;
l.ei = i;
var cx = "play", dx = "resume";
l.play = function(a) {
	if (a || this.l == 0) {
		this.Xa = 0;
		this.coords = this.cf
	} else if (this.l == 1)
		return j;
	bx(this);
	this.startTime = ya();
	if (this.l == -1)
		this.startTime -= this.duration * this.Xa;
	this.Jj = this.startTime + this.duration;
	this.ei = this.startTime;
	this.Xa || ex(this, Pw);
	ex(this, cx);
	this.l == -1 && ex(this, dx);
	this.l = 1;
	a = ra(this);
	a in Yw || (Yw[a] = this);
	Zw || (Zw = lh.setTimeout($w, 20));
	ax(this, this.startTime);
	return h
};
var fx = "stop";
l.stop = function(a) {
	bx(this);
	this.l = 0;
	if (a)
		this.Xa = 1;
	gx(this, this.Xa);
	ex(this, fx);
	ex(this, Wk)
};
l.pause = function() {
	if (this.l == 1) {
		bx(this);
		this.l = -1;
		ex(this, rf)
	}
};
var hx = "destroy";
l.h = function() {
	this.l != 0 && this.stop(j);
	ex(this, hx);
	Xw.a.h.call(this)
};
var ix = "finish", jx = "animate";
function ax(a, b) {
	a.Xa = (b - a.startTime) / (a.Jj - a.startTime);
	if (a.Xa >= 1)
		a.Xa = 1;
	a.bk = 1E3 / (b - a.ei);
	a.ei = b;
	pa(a.dj) ? gx(a, a.dj(a.Xa)) : gx(a, a.Xa);
	if (a.Xa == 1) {
		a.l = 0;
		bx(a);
		ex(a, ix);
		ex(a, Wk)
	} else
		a.l == 1 && ex(a, jx)
}
function gx(a, b) {
	a.coords = Array(a.cf.length);
	for ( var c = 0; c < a.cf.length; c++)
		a.coords[c] = (a.Cm[c] - a.cf[c]) * b + a.cf[c]
}
function ex(a, b) {
	a.dispatchEvent(new kx(b, a))
}
function kx(a, b) {
	N.call(this, a);
	this.coords = b.coords;
	this.x = b.coords[0];
	this.y = b.coords[1];
	this.Uq = b.coords[2];
	this.duration = b.duration;
	this.Xa = b.Xa;
	this.hq = b.bk;
	this.state = b.l;
	this.Mp = b
}
z(kx, N);
function lx(a) {
	return 3 * a * a - 2 * a * a * a
};
var mx = "closure_frame";
function nx() {
	this.Ld = mx + ox++;
	this.Oh = [];
	px[this.Ld] = this
}
var qx;
z(nx, Q);
var px = {}, ox = 0;
function rx(a, b) {
	Mb(b, function(c, e) {
		var f = nd(am, {
			type : Eg,
			name : e,
			value : c
		});
		a.appendChild(f)
	})
}
l = nx.prototype;
l.Z = i;
l.Fa = i;
l.Mc = i;
l.xo = 0;
l.Y = j;
l.oe = j;
l.zg = j;
l.Wa = i;
l.ci = i;
l.ya = 0;
l.Ag = 0;
l.zc = i;
l.Tj = i;
l.lc = i;
var sx = "[goog.net.IframeIo] Unable to send, already active.", tx = "zx", ux = "form", vx = "utf-8", wx = "-10px", xx = "10px";
l.send = function(a, b, c, e) {
	if (this.Y)
		d(Error(sx));
	this.Wa = a = new ei(a);
	b = b ? b.toUpperCase() : Nh;
	if (c) {
		si(a);
		c = Math.floor(Math.random() * 2147483648).toString(36)
				+ (Math.floor(Math.random() * 2147483648) ^ ya()).toString(36);
		si(a);
		delete a.jb;
		a.Ya.t(tx, c)
	}
	if (!qx) {
		qx = nd(ux);
		qx.acceptCharset = vx;
		c = qx.style;
		c.position = ng;
		c.visibility = Eg;
		c.top = c.left = wx;
		c.width = c.height = xx;
		c.overflow = Eg;
		document.body.appendChild(qx)
	}
	this.Z = qx;
	b == Nh && rx(this.Z, a.Ya);
	e && rx(this.Z, e);
	this.Z.action = a.toString();
	this.Z.method = b;
	yx(this)
};
l.abort = function(a) {
	if (this.Y) {
		ef(zx(this));
		this.zg = this.Y = this.oe = j;
		this.ya = a || 7;
		this.dispatchEvent(Of);
		Ax(this)
	}
};
l.h = function() {
	this.Y && this.abort();
	nx.a.h.call(this);
	this.Fa && Bx(this);
	Cx(this);
	delete this.Lj;
	this.Wa = this.fo = this.ci = this.eo = this.Z = i;
	this.ya = 0;
	delete px[this.Ld]
};
l.Th = function() {
	return this.oe
};
l.vk = function() {
	return this.zg
};
l.Pa = function() {
	return this.Y
};
l.xb = function() {
	return this.ci
};
l.Sf = function() {
	return this.Wa
};
l.Rf = function() {
	return zh(this.ya)
};
l.dispatchEvent = function(a) {
	try {
		return nx.a.dispatchEvent.call(this, a)
	} finally {
		return h
	}
};
var Dx = "_inner", Ex = "<body><iframe id=", Fx = " name=", Gx = "></iframe>", Hx = "load", Ix = "textarea";
function yx(a) {
	a.Y = h;
	a.oe = j;
	a.ya = 0;
	a.Mc = a.Ld + en + (a.xo++).toString(36);
	var b = {
		name : a.Mc,
		id : a.Mc
	};
	if (E && Mc < 7)
		b.src = Ql;
	a.Fa = nd(Ol, b);
	b = a.Fa.style;
	b.visibility = Eg;
	b.width = b.height = xx;
	if (G)
		b.marginTop = b.marginLeft = wx;
	else {
		b.position = ng;
		b.top = b.left = wx
	}
	if (E) {
		a.Z.target = a.Mc || A;
		document.body.appendChild(a.Fa);
		O(a.Fa, Zh, a.qi, j, a);
		try {
			a.eh = j;
			a.Z.submit()
		} catch (c) {
			af(a.Fa, Zh, a.qi, j, a);
			Jx(a, 1)
		}
	} else {
		document.body.appendChild(a.Fa);
		b = a.Mc + Dx;
		var e = Cd(a.Fa), f = Ex + b + Fx + b + Gx;
		if (Ac)
			e.documentElement.innerHTML = f;
		else
			e.write(f);
		O(e.getElementById(b), Hx, a.ng, j, a);
		var g = a.Z.getElementsByTagName(Ix);
		f = 0;
		for ( var k = g.length; f < k; f++)
			Ld(g[f]) != g[f].value && Ed(g[f], g[f].value);
		g = e.importNode(a.Z, h);
		g.target = b;
		e.body.appendChild(g);
		var m = a.Z.getElementsByTagName(kl), o = g.getElementsByTagName(kl);
		f = 0;
		for (k = m.length; f < k; f++)
			o[f].selectedIndex = m[f].selectedIndex;
		m = a.Z.getElementsByTagName(am);
		o = g.getElementsByTagName(am);
		f = 0;
		for (k = m.length; f < k; f++)
			if (m[f].type == Vi)
				if (m[f].value != o[f].value) {
					a.Z.target = b;
					g = a.Z;
					break
				}
		try {
			a.eh = j;
			g.submit();
			e.close();
			if (F)
				a.Tj = V(a.zl, 250, a)
		} catch (q) {
			af(e.getElementById(b), Hx, a.ng, j, a);
			e.close();
			Jx(a, 2)
		}
	}
}
var Kx = "about:blank";
l.qi = function() {
	if (this.Fa.readyState == Xh) {
		af(this.Fa, Zh, this.qi, j, this);
		var a;
		try {
			a = Cd(this.Fa);
			if (E && a.location == Kx && !navigator.onLine) {
				Jx(this, 9);
				return
			}
		} catch (b) {
			Jx(this, 1);
			return
		}
		Lx(this, a)
	}
};
l.ng = function() {
	if (!(Ac && Mx(this).location == Kx)) {
		af(zx(this), Hx, this.ng, j, this);
		Lx(this, Mx(this))
	}
};
function Lx(a, b) {
	a.oe = h;
	a.Y = j;
	var c;
	try {
		var e = b.body;
		a.ci = e.textContent || e.innerText;
		a.eo = e.innerHTML
	} catch (f) {
		c = 1
	}
	var g;
	if (!c && typeof a.Lj == s)
		if (g = a.Lj(b))
			c = 4;
	if (c)
		Jx(a, c, g);
	else {
		a.zg = h;
		a.ya = 0;
		a.dispatchEvent(Xh);
		a.dispatchEvent(S);
		Ax(a)
	}
}
function Jx(a, b, c) {
	if (!a.eh) {
		a.zg = j;
		a.Y = j;
		a.oe = h;
		a.ya = b;
		if (b == 4)
			a.fo = c;
		a.dispatchEvent(Xh);
		a.dispatchEvent(T);
		Ax(a);
		a.eh = h
	}
}
function Ax(a) {
	Bx(a);
	Cx(a);
	a.dispatchEvent(di)
}
function Bx(a) {
	var b = a.Fa;
	if (b) {
		b.onreadystatechange = i;
		b.onload = i;
		b.onerror = i;
		a.Oh.push(b)
	}
	if (a.lc) {
		lh.clearTimeout(a.lc);
		a.lc = i
	}
	if (F || Ac)
		a.lc = V(a.$g, 2E3, a);
	else
		a.$g();
	a.Fa = i;
	a.Mc = i
}
l.$g = function() {
	if (this.lc) {
		lh.clearTimeout(this.lc);
		this.lc = i
	}
	for ( var a = 0; a < this.Oh.length;)
		a++;
	if (this.Oh.length != 0)
		this.lc = V(this.$g, 2E3, this)
};
function Cx(a) {
	a.Z && a.Z == qx && wd(a.Z);
	a.Z = i
}
function Mx(a) {
	if (a.Fa)
		return Cd(zx(a));
	return i
}
function zx(a) {
	if (a.Fa)
		return E ? a.Fa : Cd(a.Fa).getElementById(a.Mc + Dx);
	return i
}
l.zl = function() {
	if (this.Y) {
		var a = Mx(this);
		if (a)
			try {
				qe(a.documentUri)
			} catch (b) {
				af(zx(this), Hx, this.ng, j, this);
				navigator.onLine ? Jx(this, 3) : Jx(this, 9);
				return
			}
		this.Tj = V(this.zl, 250, this)
	}
};
function Nx(a, b, c) {
	Ff.call(this, a);
	this.Z = b;
	this.I = i;
	this.df = 15E3;
	this.Im = 5E3;
	this.gj = 500;
	this.Te = i;
	this.Cc = c || 0;
	this.hi = this.ii = 0
}
z(Nx, Ff);
l = Nx.prototype;
l.abort = function() {
	this.I.abort();
	this.dispatchEvent(Of)
};
l.yc = function(a) {
	var b = new nx;
	this.I = b;
	O(b, S, w(this.jn, this));
	O(b, T, w(this.hn, this));
	var c = this.Z;
	if (b.Y)
		d(Error(sx));
	a = new ei(a || c.action);
	b.Wa = a;
	b.Z = c;
	b.Z.action = a.toString();
	yx(b);
	this.df > 0 && V(this.ug, this.Im, this);
	this.hi = (new Date).getTime();
	this.dispatchEvent(pf)
};
l.jn = function() {
	try {
		var a = this.I.xb(), b = (a && ae(a)).sessionStatus.externalFieldTransfers[0], c = this
				.Da();
		if (c.Na == i)
			c.Na = b.bytesTotal
	} catch (e) {
	}
	this.Lc(this.I.xb() || A)
};
l.hn = function() {
	this.handleError(Ox(this, this.I.ya), this.I.Rf())
};
function Ox(a, b) {
	switch (b) {
	case 0:
		return Ef;
	case 5:
		return Rf;
	case 6:
		return rj;
	case 7:
		return Of;
	case 8:
		return Uh;
	case 1:
		return xj;
	default:
		return Vf
	}
}
l.ug = function() {
	if (this.I.Pa) {
		var a = new Lh;
		O(a, S, w(this.In, this));
		O(a, T, w(this.Hn, this));
		try {
			var b;
			b = this.Da().ie;
			a.send(b.substring(0, b.length - 2), Nh)
		} catch (c) {
		}
	}
};
l.In = function(a) {
	if (this.I.Pa()) {
		try {
			var b = ae(a.target.xb()).sessionStatus.externalFieldTransfers[0], c = b.bytesTransferred, e = this
					.Da();
			if (e.Na == i)
				if (b.bytesTotal > 0)
					e.Na = b.bytesTotal;
				else {
					V(this.ug, this.df, this);
					return
				}
			var f = e.Na;
			this.Te != i && this.Te.stop(j);
			var g = (new Date).getTime();
			if (this.ii > 0 || this.Cc == 0)
				this.Cc = Math.round((c - this.ii) / (g - this.hi));
			this.ii = c;
			this.hi = g;
			var k = this.df + this.gj, m = e.Kb, o = Math
					.round(c + k * this.Cc);
			if (o > f) {
				o = f;
				k = Math.round((f - c) / this.Cc) + this.gj
			}
			this.Te = new Xw([ m ], [ o ], k, lx);
			O(this.Te, jx, w(function(x) {
				Kf(this, Math.round(x.x))
			}, this));
			this.Te.play()
		} catch (q) {
		}
		this.I.Pa && V(this.ug, this.df, this)
	}
};
l.Hn = function() {
	this.I.Pa && V(this.ug, this.df, this)
};
function Px() {
	Zi.call(this);
	this.oh = new D;
	this.Cc = 0;
	V(function() {
		this.Hb(j)
	}, 1, this)
}
z(Px, Zi);
l = Px.prototype;
var Qx = "scotty html form";
l.Ad = function() {
	return Qx
};
l.Va = function() {
	var a = Px.a.Va.call(this);
	return fb(a, function(b) {
		return !this.Vh(b)
	}, this)
};
l.Vh = function(a) {
	return a.fa == A
};
l.nf = function() {
};
var Rx = "Filedata", Sx = "uploader-upload-input", Tx = "multipart/form-data";
function Ux(a, b, c) {
	var e = K(b), f = e.d(am, {
		type : Vi,
		name : Rx,
		size : 20,
		"class" : Sx
	});
	e = e.d(ux, {
		method : Oh,
		enctype : Tx,
		encoding : Tx
	}, f);
	b.appendChild(e);
	a.oh.t(c.k(), e);
	O(f, Fs, w(function(g) {
		c.Wd(Vx(this, g.target.value));
		this.dispatchEvent(new Zg(zk, [ c ]));
		this.Og && this.vc(c);
		this.sc && !hb(this.gc, this.Vh, this) && this.Hb(j)
	}, a))
}
function Vx(a, b) {
	if (b.match(/^c:\\fakepath\\/i))
		b = b.substring(12);
	return b
}
l.nd = function(a) {
	var b = this.oh.w(a.k());
	return new Nx(a, b, this.Cc)
};
l.ti = function(a) {
	this.Cc = a.target.Cc;
	Px.a.ti.call(this, a)
};
l.Hb = function(a) {
	if (this.Ib)
		a ? Dj(this) : this.Ec(new lf)
};
l.Wb = function(a) {
	Px.a.Wb.call(this, a);
	this.oh.remove(a.k());
	this.gc.length == 0 && this.Hb(j)
};
function Wx(a) {
	C(ob(a.gc), function(b) {
		this.Vh(b) && this.Wb(b)
	}, a)
}
l.Sd = function(a) {
	Px.a.Sd.call(this, a);
	a || Wx(this)
};
function Xx(a, b) {
	Ff.call(this, a);
	this.Kf = b
}
z(Xx, Ff);
Xx.prototype.I = i;
Xx.prototype.yc = function(a) {
	var b = new Yx;
	this.I = b;
	O(b, S, function() {
		this.Lc(b.xb())
	}, j, this);
	O(b, T, function() {
		this.handleError(b.ya, b.Rf())
	}, j, this);
	O(b, Lf, function(c) {
		Kf(this, c.Ja.loaded)
	}, j, this);
	b.send(a, Oh, this.Kf, {
		"Content-Type" : this.Kf.type || iw
	});
	this.dispatchEvent(pf)
};
Xx.prototype.abort = function() {
	this.I.abort();
	this.dispatchEvent(Of)
};
function Yx() {
	Lh.call(this)
}
z(Yx, Lh);
Yx.prototype.Vg = function() {
	var a = Yx.a.Vg.call(this);
	O(a.upload, Lf, this.dispatchEvent, j, this);
	return a
};
function Zx(a, b) {
	Yv.call(this, a);
	this.Kf = b
}
z(Zx, Yv);
Zx.prototype.oj = function() {
	var a = new Yx;
	O(a, Zh, this.Ih, j, this);
	O(a, Lf, function(b) {
		var c = this.Yb;
		Kf(this, (c ? c.start : 0) + b.Ja.loaded)
	}, j, this);
	return a
};
Zx.prototype.nl = function(a, b, c) {
	var e = this.Kf;
	if (c)
		e = e.slice(c.start, c.end - c.start + 1);
	this.I.send(a, Oh, e, b)
};
var $x = "network error";
Zx.prototype.Fe = function() {
	if (this.Vf() == 0)
		this.gd ? tw(this) : this.handleError(sj, $x);
	else
		Zx.a.Fe.call(this)
};
function ay(a) {
	if (!a)
		d(Error(hu));
	Zi.call(this);
	this.zq = a;
	this.ub = j;
	this.ai = undefined;
	this.rg = new D;
	this.Cg = new D;
	this.Dq = new Xg;
	V(function() {
		by(this, a);
		this.af(this.sc);
		this.Td($i(this))
	}, undefined, this)
}
z(ay, Zi);
l = ay.prototype;
var cy = "scotty xhr", dy = "scotty xhr resumable", ey = "scotty xhr non-resumable";
l.Ad = function() {
	return this.ai == undefined ? cy : this.ai ? dy : ey
};
l.cg = function() {
	return (this.Ka || nd(am, {
		type : Vi
	})).webkitdirectory != undefined
};
l.Oc = function() {
	return h
};
var fy = "height:0;visibility:hidden;position:absolute", gy = "opacity:0", hy = "overflow:hidden;width:", iy = "px;height:", jy = "background:red";
function by(a, b) {
	if (!a.Ka) {
		var c = Wc(b), e = K(c);
		if (G) {
			a.Ka = nd(am, {
				type : Vi,
				style : fy
			});
			e.Sh(a.Ka, c)
		} else {
			a.Ka = nd(am, {
				type : Vi,
				style : gy
			});
			J(c, ju);
			var f = Gg(c);
			f = e.d(L, {
				style : hy + f.width + iy + f.height + jy
			}, a.Ka);
			e.Sh(f, c);
			bg(f, gg, ng);
			bg(c.parentNode, gg, lu);
			jg(f, new lc(c.offsetLeft, c.offsetTop))
		}
		O(a.Ka, Fs, function() {
			ky(this, this.Ka.files);
			this.Ka.value = A
		}, j, a)
	}
}
var ly = "Firefox/3.6";
l.Uh = function() {
	return wc().indexOf(ly) >= 0 && !vc ? h : G && !Oj
};
l.ej = function(a) {
	(a = (a = a.Ja.dataTransfer) && a.files) && ky(this, fb(a, function(b) {
		return (b.size || b.fileSize) > 0 && b.type != A
	}))
};
l.nd = function(a) {
	var b = W(this, a).ak;
	if (b.slice) {
		a = new Zx(a, b);
		a.Fi(h);
		return a
	} else
		return new Xx(a, b)
};
l.Hb = function(a) {
	if (this.Ib) {
		this.ub = a;
		this.Ka.click()
	}
};
var my = "multiple";
l.af = function(a) {
	ay.a.af.call(this, a);
	if (this.Ka)
		if (this.sc)
			this.Ka.multiple = ac;
		else
			this.Ka.removeAttribute(my)
};
var ny = "webkitdirectory";
l.Td = function(a) {
	ay.a.Td.call(this, a);
	if (this.Ka)
		if ($i(this))
			this.Ka.webkitdirectory = ac;
		else
			this.Ka.removeAttribute(ny)
};
function ky(a, b) {
	if (b.length != 0) {
		a.ub && Dj(a);
		var c = gb(b, function(e) {
			var f = $i(this) ? e.webkitRelativePath : e.name || e.fileName;
			f = Jj(f);
			return new lf(f, e.size || e.fileSize)
		}, a);
		a.ai = !!b[0].slice;
		C(c, function(e, f) {
			W(this, e).ak = b[f]
		}, a);
		if ($i(a))
			c = [ Ij(c) ];
		a.dispatchEvent(new Zg(zk, c));
		C(c, function(e) {
			this.Ec(e)
		}, a)
	}
}
l.fk = function() {
	return this.Ka
};
l.vc = function(a) {
	this.rg.Oa(a.k()) ? this.Cg.t(a.k(), h) : ay.a.vc.call(this, a)
};
function oy() {
}
z(oy, Q);
l = oy.prototype;
l.Ba = 0;
l.Ne = 0;
l.ni = 100;
l.Nj = 0;
l.Pi = 1;
l.Un = j;
l.to = j;
l.Ma = function(a) {
	a = py(this, a);
	if (this.Ba != a) {
		this.Ba = a + this.Nj > this.ni ? this.ni - this.Nj
				: a < this.Ne ? this.Ne : a;
		!this.Un && !this.to && this.dispatchEvent(Fs)
	}
};
l.M = function() {
	return py(this, this.Ba)
};
l.Uf = function() {
	return py(this, this.Ne)
};
l.Tf = function() {
	return py(this, this.ni)
};
function py(a, b) {
	if (a.Pi == i)
		return b;
	return a.Ne + Math.round((b - a.Ne) / a.Pi) * a.Pi
};
function qy(a) {
	Y.call(this, a);
	this.Od = new oy;
	O(this.Od, Fs, this.Xm, j, this)
}
z(qy, Y);
var ry = {};
ry.vertical = "progress-bar-vertical";
ry.horizontal = "progress-bar-horizontal";
l = qy.prototype;
var sy = "valuemin", ty = "valuemax";
l.d = function() {
	this.bb = uy(this);
	var a = ry[this.Qa];
	this.m = this.g().d(L, a, this.bb);
	vy(this);
	X(this.b(), sy, this.Uf());
	X(this.b(), ty, this.Tf())
};
var wy = "progressbar", xy = "live", yy = "polite";
l.B = function() {
	qy.a.B.call(this);
	this.Mg();
	this.jf();
	Fk(this.b(), wy);
	X(this.b(), xy, yy)
};
l.P = function() {
	qy.a.P.call(this);
	this.Bf()
};
var zy = "progress-bar-thumb";
function uy(a) {
	return a.g().d(L, zy)
}
l.Mg = function() {
	E && Mc < 7 && O(this.b(), Xl, this.jf, j, this)
};
l.Bf = function() {
	E && Mc < 7 && af(this.b(), Xl, this.jf, j, this)
};
l.Ua = function(a) {
	qy.a.Ua.call(this, a);
	J(this.b(), ry[this.Qa]);
	a = Xc(i, zy, this.b())[0];
	if (!a) {
		a = uy(this);
		this.b().appendChild(a)
	}
	this.bb = a
};
l.M = function() {
	return this.Od.M()
};
l.Ma = function(a) {
	this.Od.Ma(a);
	this.b() && vy(this)
};
var Ay = "valuenow";
function vy(a) {
	X(a.b(), Ay, a.M())
}
l.Uf = function() {
	return this.Od.Uf()
};
l.Tf = function() {
	return this.Od.Tf()
};
l.Qa = Do;
l.Xm = function() {
	this.jf();
	this.dispatchEvent(Fs)
};
var By = "100%";
l.jf = function() {
	if (this.bb) {
		var a = this.Uf(), b = this.Tf();
		a = (this.M() - a) / (b - a);
		b = Math.round(a * 100);
		if (this.Qa == Fo)
			if (E && Mc < 7) {
				this.bb.style.top = 0;
				this.bb.style.height = By;
				b = this.bb.offsetHeight;
				a = Math.round(a * b);
				this.bb.style.top = b - a + Ag;
				this.bb.style.height = a + Ag
			} else {
				this.bb.style.top = 100 - b + xi;
				this.bb.style.height = b + xi
			}
		else
			this.bb.style.width = b + xi
	}
};
l.Hi = function(a) {
	if (this.Qa != a) {
		var b = ry[this.Qa], c = ry[a];
		this.Qa = a;
		if (this.b()) {
			a = this.b();
			for ( var e = Sc(a), f = j, g = 0; g < e.length; g++)
				if (e[g] == b) {
					rb(e, g--, 1);
					f = h
				}
			if (f) {
				e.push(c);
				a.className = e.join(I)
			}
			b = this.bb.style;
			if (this.Qa == Fo) {
				b.left = 0;
				b.width = By
			} else {
				b.top = b.left = 0;
				b.height = By
			}
			this.jf()
		}
	}
};
l.h = function() {
	this.Bf();
	qy.a.h.call(this);
	this.bb = i;
	this.Od.u()
};
function Cy() {
}
z(Cy, on);
r(Cy);
l = Cy.prototype;
l.d = function(a) {
	var b = {
		"class" : co + this.kc(a).join(I),
		title : a.Gd() || A
	};
	return a.g().d(L, b, a.Ca)
};
l.nb = function() {
	return $l
};
l.Ha = function(a) {
	return a.tagName == Tl
};
l.G = function(a, b) {
	J(b, go);
	return Cy.a.G.call(this, a, b)
};
l.M = function() {
	return i
};
var Dy = "goog-flat-button";
l.o = function() {
	return Dy
};
zn(Dy, function() {
	return new ao(i, Cy.O())
});
function Ey() {
}
z(Ey, Rm);
var Fy = i;
l = Ey.prototype;
var Gy = "tr";
l.d = function(a) {
	var b = this.kc(a);
	b = a.g().d(Gy, b ? {
		"class" : b.join(I)
	} : i);
	this.dc(a, b);
	return b
};
l.dc = function() {
};
l.Ha = function(a) {
	return a.tagName.toLowerCase() == Gy
};
l.G = function(a, b) {
	this.dc(a, b);
	return Ey.a.G.call(this, a, b)
};
var Hy = "upload-state-default", Iy = "upload-state-inqueue", Jy = "upload-state-start", Ky = "upload-state-upload", Ly = "upload-state-pause", My = "upload-state-backoff", Ny = "upload-state-complete", Oy = "upload-state-error", Py = "upload-state-cancel";
l.Mf = function(a) {
	switch (a) {
	case nf:
		return Hy;
	case of:
		return Iy;
	case pf:
		return Jy;
	case qf:
		return Ky;
	case rf:
		return Ly;
	case sf:
		return My;
	case S:
		return Ny;
	case T:
		return Oy;
	case uf:
		return Py;
	default:
		return A
	}
};
l.ka = function(a) {
	Ey.a.ka.call(this, a);
	Tg(a.b(), j, F);
	Qy(a, A);
	Ry(a).A(j);
	Sy(a).A(j)
};
l.Ji = function() {
};
l.Gb = function(a) {
	var b = a.oi;
	a = Ty(a);
	if (b) {
		a.$a(Xb + b + Zd);
		a.A(h)
	} else {
		a.$a(A);
		a.A(j)
	}
};
l.Ta = function(a, b, c) {
	Fn(a, this.Mf(b));
	En(a, this.Mf(c));
	Uy(a).A(c == nf || c == of);
	Ry(a).A(c == pf || c == qf || c == sf)
};
var Vy = "GB", Wy = "MB", Xy = "K";
function Yy(a, b) {
	var c, e, f, g;
	if (b > 1073741824) {
		e = b / 1073741824;
		g = Vy
	} else if (b > 1048576) {
		e = b / 1048576;
		g = Wy
	} else {
		e = b > 1024 ? b / 1024 : gk;
		g = Xy
	}
	f = Math.floor(e);
	if (e = (c = (c || 3) - (A + f).length) ? e - f : 0)
		e = Math.round(e * Math.pow(10, c));
	if (e == 10) {
		f++;
		e = 0
	}
	return (e ? f + ba + e : f) + I + g
}
var Zy = "upload-file";
l.o = function() {
	return Zy
};
function $y(a, b, c) {
	$.call(this, a, b || Fy || (Fy = new Ey), c);
	this.Fc = new D;
	this.Ud(h)
}
z($y, $);
$y.prototype.dispatchEvent = function(a) {
	try {
		return $y.a.dispatchEvent.call(this, a)
	} catch (b) {
	}
};
var az = {
	bj : nf,
	Pl : of,
	Tl : pf,
	Hg : qf,
	Rl : rf,
	aj : sf,
	Ul : S,
	Ql : tf,
	Nl : T,
	Ml : uf
};
l = $y.prototype;
l.bd = nf;
l.xl = i;
l.Dk = i;
l.sl = i;
l.Zk = i;
l.mj = i;
l.fl = i;
l.Bk = i;
l.oi = A;
function bz(a) {
	return a.xl || (a.xl = new $(A, undefined, a.g()))
}
function cz(a) {
	return a.Dk || (a.Dk = new $(A, undefined, a.g()))
}
function dz(a) {
	return a.sl || (a.sl = new $(A, undefined, a.g()))
}
function Uy(a) {
	return a.Zk || (a.Zk = new ao(i, Cy.O(), a.g()))
}
function Ry(a) {
	return a.mj || (a.mj = new ao(i, Cy.O(), a.g()))
}
function Sy(a) {
	return a.fl || (a.fl = new ao(i, Cy.O(), a.g()))
}
function Ty(a) {
	return a.Bk || (a.Bk = new $(A, undefined, a.g()))
}
l.Ma = function(a) {
	this.Rc = a;
	this.Gb()
};
l.M = function() {
	return this.Rc
};
l.Gb = function() {
	this.q.Gb(this)
};
l.Ta = function(a) {
	if (a != this.bd)
		if (Cb(az, a)) {
			this.M();
			var b = this.bd;
			this.bd = a;
			this.q.Ta(this, b, a);
			return h
		} else
			d(Error("Unsupported upload state: " + a));
	else
		return j
};
function Qy(a, b) {
	if (b != a.oi) {
		a.oi = b;
		a.Gb()
	}
}
l.Ji = function(a, b) {
	this.q.Ji(this, a, b)
};
l.Vb = function(a) {
	if (a.target != this.b()) {
		var b = a.target.id;
		this.Fc.Oa(b) && this.Fc.w(b)(a)
	}
	return $y.a.Vb.call(this, a)
};
var ez = ".file";
l.B = function() {
	$y.a.B.call(this);
	this.b().id = this.k() + ez;
	this.q.Ta(this, this.bd, this.bd);
	var a = Uy(this), b = w(this.wn, this);
	this.Fc.t(a.k(), b);
	a = Ry(this);
	b = w(this.Um, this);
	this.Fc.t(a.k(), b);
	a = Sy(this);
	b = w(this.An, this);
	this.Fc.t(a.k(), b)
};
var fz = "removeclicked";
l.wn = function() {
	this.dispatchEvent(fz)
};
var gz = "cancelclicked";
l.Um = function() {
	this.dispatchEvent(gz)
};
l.An = function() {
};
zn(Zy, function() {
	return new $y(i)
});
function hz() {
}
z(hz, Ey);
var iz = i, jz = "-col", kz = "td", lz = "-status", mz = "-status apps-upload-sprite", nz = "-name", oz = "-message", pz = "-size", qz = "-progress", rz = "-actions", sz = "Retry", tz = "Remove";
hz.prototype.dc = function(a, b) {
	var c = a.g(), e = this.o(), f = e + jz;
	c = [ c.d(kz, {
		"class" : f + I + (e + lz)
	}, c.d(L, {
		"class" : e + mz,
		id : bz(a).k()
	})), c.d(kz, {
		"class" : f + I + (e + nz)
	}, c.d(Gl, {
		id : cz(a).k()
	}), c.d(Gl, {
		id : Ty(a).k(),
		"class" : e + oz
	})), c.d(kz, {
		"class" : f + I + (e + pz)
	}, c.d(Gl, {
		id : dz(a).k()
	})), c.d(kz, {
		"class" : f + I + (e + qz)
	}, c.d(L, {
		id : uz(a).k()
	})), c.d(kz, {
		"class" : f + I + (e + rz)
	}, c.d(L, {
		id : Sy(a).k()
	}, sz), c.d(L, {
		id : Uy(a).k()
	}, tz), c.d(L, {
		id : Ry(a).k()
	}, mm)) ];
	C(c, function(g) {
		b.appendChild(g)
	})
};
hz.prototype.ka = function(a) {
	var b = [ bz(a), cz(a), dz(a), uz(a), Ty(a), Uy(a), Ry(a), Sy(a) ], c = a
			.g();
	C(b, function(e) {
		e.G(c.b(e.k()))
	});
	hz.a.ka.call(this, a)
};
hz.prototype.Gb = function(a) {
	hz.a.Gb.call(this, a);
	var b = a.M();
	if (b) {
		cz(a).Ze(b.getName());
		vz(iz || (iz = new hz), a)
	}
};
function vz(a, b) {
	var c = b.M(), e = 0, f = c.Na;
	if (f == i)
		dz(b).Ze(A);
	else {
		dz(b).Ze(Yy(a, f));
		e = Math.round(c.Kb / f * 100)
	}
	uz(b).Ma(e)
}
var wz = "-hidden";
hz.prototype.Ta = function(a, b, c) {
	hz.a.Ta.call(this, a, b, c);
	c == T && Fn(Sy(a), this.o() + wz)
};
function xz() {
}
z(xz, hz);
r(xz);
var yz = "upload-state-default-dir", zz = "upload-state-inqueue-dir", Az = "upload-state-start-dir", Bz = "upload-state-upload-dir", Cz = "upload-state-part-error-dir", Dz = "upload-state-complete-dir", Ez = "upload-state-backoff-dir";
xz.prototype.Mf = function(a) {
	switch (a) {
	case nf:
		return yz;
	case of:
		return zz;
	case pf:
		return Az;
	case qf:
		return Bz;
	case tf:
		return Cz;
	case S:
		return Dz;
	case sf:
		return Ez;
	default:
		return xz.a.Mf.call(this, a)
	}
};
var Fz = "-filecount";
xz.prototype.dc = function(a, b) {
	var c = a.g(), e = this.o(), f = e + jz;
	c = [ c.d(kz, {
		"class" : f + I + (e + lz)
	}, c.d(L, {
		"class" : e + mz,
		id : bz(a).k()
	})), c.d(kz, {
		"class" : f + I + (e + nz)
	}, c.d(Gl, {
		id : cz(a).k()
	}), c.d(Gl, {
		id : Gz(a).k(),
		"class" : e + Fz
	}), c.d(Gl, {
		id : Ty(a).k(),
		"class" : e + oz
	})), c.d(kz, {
		"class" : f + I + (e + pz)
	}, c.d(Gl, {
		id : dz(a).k()
	})), c.d(kz, {
		"class" : f + I + (e + qz)
	}, c.d(L, {
		id : uz(a).k()
	})), c.d(kz, {
		"class" : f + I + (e + rz)
	}, c.d(L, {
		id : Sy(a).k()
	}, sz), c.d(L, {
		id : Uy(a).k()
	}, tz), c.d(L, {
		id : Ry(a).k()
	}, mm)) ];
	C(c, function(g) {
		b.appendChild(g)
	})
};
xz.prototype.ka = function(a) {
	var b = Gz(a);
	b.G(a.g().b(b.k()));
	xz.a.ka.call(this, a)
};
xz.prototype.Gb = function(a) {
	hz.a.Gb.call(this, a);
	var b = a.M();
	if (b) {
		cz(a).Ze(b.getName());
		vz(iz || (iz = new hz), a);
		b = Hz(a);
		a = Gz(a);
		a.$a(b ? b : A);
		a.A(!!b)
	}
};
function Iz(a, b, c) {
	$y.call(this, a, b || iz || (iz = new hz), c)
}
z(Iz, $y);
Iz.prototype.Vk = i;
function uz(a) {
	return a.Vk || (a.Vk = new qy(a.g()))
}
Iz.prototype.Si = function() {
	vz(this.q, this)
};
function Jz(a, b, c, e, f) {
	Iz.call(this, c, e || xz.O(), f);
	this.p = a;
	this.Gc = b;
	this.Qj = new D;
	this.xe = this.ld = 0
}
z(Jz, Iz);
function Gz(a) {
	return a.Fm || (a.Fm = new $(A, undefined, a.g()))
}
var Kz = " files)", Lz = " succeeded, ", Mz = " failed)", Nz = " of ";
function Hz(a) {
	switch (a.bd) {
	case nf:
	case S:
	case uf:
	case T:
		return Xb + (a.Gc.ze + Kz);
	case tf:
		return Xb + (a.ld - a.xe + (Lz + (a.xe + Mz)));
	case of:
	case pf:
	case qf:
		return Xb + (a.ld + (Nz + (a.Gc.ze + Zd)));
	default:
		return i
	}
};
function Oz() {
	Pz()
}
z(Oz, Ao);
r(Oz);
var Qz = "6";
function Pz() {
	if (E && Mc.split(ba, 2)[0] == Qz)
		try {
			document.execCommand(Cs, j, h)
		} catch (a) {
		}
}
Oz.prototype.Ha = function() {
	return j
};
Oz.prototype.ka = function(a) {
	Oz.a.ka.call(this, a);
	Tg(a.b(), j, F)
};
var Rz = "upload-uploader";
Oz.prototype.o = function() {
	return Rz
};
function Sz() {
	Pz()
}
z(Sz, Oz);
r(Sz);
var Tz = "-inner", Uz = "-file-list", Vz = "table", Wz = "-table", Xz = "tbody", Yz = "-footer";
Sz.prototype.d = function(a) {
	var b = a.g(), c = this.o();
	return b.d(L, {
		"class" : c
	}, b.d(L, {
		"class" : c + Tz
	}, b.d(L, {
		"class" : c + Uz
	}, b.d(Vz, {
		"class" : c + Wz
	}, b.d(Xz))), b.d(L, {
		"class" : c + Yz,
		id : (a.Jf || (a.Jf = new $(A, undefined, a.g()))).k()
	})))
};
Sz.prototype.Q = function(a) {
	return Xc(Xz, i, a)[0]
};
Sz.prototype.ka = function(a) {
	Sz.a.ka.call(this, a);
	var b = a.Jf || (a.Jf = new $(A, undefined, a.g()));
	b.G(a.g().b(b.k()))
};
function Zz() {
}
z(Zz, hz);
var $z = i, aA = "-form", bA = "id";
Zz.prototype.dc = function(a, b) {
	var c = a.g();
	Zz.a.dc.call(this, a, b);
	var e = c.d(Gl, {
		"class" : this.o() + aA,
		id : cA(a).k()
	}), f = Od(c, Gl, A, b);
	c = jb(f, function(g) {
		return g.getAttribute(bA) == cz(a).k()
	});
	f = jb(f, function(g) {
		return g.getAttribute(bA) == dz(a).k()
	});
	c.parentNode && c.parentNode.insertBefore(e, c);
	U(c, j);
	U(f, j)
};
Zz.prototype.ka = function(a) {
	var b = cA(a);
	b.G(a.g().b(b.k()));
	Zz.a.ka.call(this, a)
};
Zz.prototype.Ta = function(a, b, c) {
	Zz.a.Ta.call(this, a, b, c);
	switch (c) {
	case pf:
		cA(a).A(j);
		U(cz(a).b(), h);
		U(dz(a).b(), h)
	}
};
function dA(a, b, c, e) {
	Iz.call(this, b, c || $z || ($z = new Zz), e);
	this.p = a
}
z(dA, Iz);
dA.prototype.$j = i;
function cA(a) {
	return a.$j || (a.$j = new $(A, undefined, a.g()))
}
dA.prototype.fk = function() {
	return Od(this.Ob, am, i, cA(this).b())[0]
};
dA.prototype.Ma = function(a) {
	dA.a.Ma.call(this, a);
	Ux(this.p, cA(this).b(), this.M());
	a = this.ja();
	var b = this.fk(), c = w(this.fn, this);
	a.e(b, Fs, function() {
		V(c)
	})
};
dA.prototype.fn = function() {
	if (this.C) {
		this.Gb();
		this.dispatchEvent(Fs)
	}
};
function eA(a) {
	Y.call(this, a)
}
z(eA, Y);
l = eA.prototype;
l.cd = i;
l.Ll = i;
l.nk = i;
l.Ph = i;
l.ck = i;
var fA = "auto", gA = "uploader_image_frame_", hA = 'javascript:"<html><body></body></html>"', iA = "upload-thumbnail-frame", jA = "margin", kA = "-15px -10px 0", lA = "post", mA = "//uploaded-thumbnails.googleusercontent.com/processimage", nA = "image", oA = "img", pA = "uploader-thumb-img";
l.B = function() {
	eA.a.B.call(this);
	if (this.cd) {
		var a = this.b(), b = this.Ll, c = this.nk;
		if (b && c)
			zg(a, b, c);
		else {
			a.style.height = fA;
			a.style.width = fA
		}
		var e = this.cd;
		if (e.match(/^data:/) && E && cb(Mc, te) < 0) {
			b = this.g();
			a = this.b();
			c = this.ck;
			if (!c) {
				qA(this);
				var f = gA + this.k();
				c = b.d(Ol, {
					src : hA,
					name : f,
					id : f,
					"class" : iA,
					frameBorder : Ya,
					scrolling : pm,
					allowtransparency : ac
				});
				e = b.d(L, i, c);
				a.appendChild(e)
			}
			e = this.Ll;
			var g = this.nk;
			e && g && zg(c, e + 20, g + 15);
			bg(c, jA, kA);
			c = this.cd.replace(/^data:.*?,/, A);
			b = b.d(ux, {
				method : lA,
				action : mA
			}, b.d(am, {
				type : Eg,
				name : nA,
				value : c
			}));
			a.appendChild(b);
			b.target = f;
			b.submit()
		} else {
			if (f = this.Ph)
				f.src = e;
			else {
				qA(this);
				f = this.Ph = this.g().d(oA, {
					src : e,
					"class" : pA
				});
				a.appendChild(f)
			}
			b && c && zg(f, b, c);
			if (E && H(Tm)) {
				a.style.height = fA;
				a.style.width = fA
			}
		}
	}
};
function qA(a) {
	wd(a.b());
	a.Ph = i;
	a.ck = i
};
function rA(a, b, c) {
	Iz.call(this, a, b, c)
}
z(rA, Iz);
function sA() {
}
z(sA, hz);
var tA = i, uA = "Pause", vA = "Resume";
sA.prototype.dc = function(a, b) {
	var c = a.g();
	sA.a.dc.call(this, a, b);
	var e = c.d(L, {
		id : wA(a).k()
	}, uA), f = c.d(L, {
		id : xA(a).k()
	}, vA);
	c = Od(c, L, A, b);
	var g = Ry(a).k();
	c = jb(c, function(k) {
		return k.getAttribute(bA) == g
	});
	c.parentNode && c.parentNode.insertBefore(e, c);
	xd(f, e)
};
sA.prototype.ka = function(a) {
	var b = a.g();
	C([ wA(a), xA(a) ], function(c) {
		c.G(b.b(c.k()))
	});
	wA(a).A(j);
	xA(a).A(j);
	sA.a.ka.call(this, a)
};
sA.prototype.Ta = function(a, b, c) {
	sA.a.Ta.call(this, a, b, c)
};
function yA(a, b, c) {
	Iz.call(this, a, b || tA || (tA = new sA), c)
}
z(yA, Iz);
l = yA.prototype;
l.Sk = i;
l.dl = i;
function wA(a) {
	return a.Sk || (a.Sk = new ao(i, Cy.O(), a.g()))
}
function xA(a) {
	return a.dl || (a.dl = new ao(i, Cy.O(), a.g()))
}
l.B = function() {
	yA.a.B.call(this);
	var a = wA(this), b = w(this.tn, this);
	this.Fc.t(a.k(), b);
	a = xA(this);
	b = w(this.yn, this);
	this.Fc.t(a.k(), b)
};
var zA = "pauseclicked";
l.tn = function() {
	this.dispatchEvent(zA)
};
var AA = "resumeclicked";
l.yn = function() {
	this.dispatchEvent(AA)
};
function BA(a, b, c, e) {
	Io.call(this, i, b || Sz.O(), e);
	this.hh = new D;
	if (a instanceof hk)
		this.xd = function(f, g) {
			return f instanceof cj ? new Jz(a, f, i, g, e) : new yA(i, g, e)
		};
	else if (a instanceof Sw)
		this.xd = function(f, g) {
			return new yA(i, g, e)
		};
	else if (a instanceof qu)
		this.xd = function(f, g) {
			return new rA(i, g, e)
		};
	else if (a instanceof ay)
		this.xd = function(f, g) {
			return f instanceof cj ? new Jz(a, f, i, g, e) : new rA(i, g, e)
		};
	else if (a instanceof Px)
		this.xd = function(f, g) {
			return new dA(a, i, g, e)
		};
	else if (a instanceof Zi)
		this.xd = function(f, g) {
			return new $y(i, g, e)
		};
	else
		d(Error("unrecognized uploader type"));
	this.p = a;
	this.Gm = c || i;
	this.Xb(j);
	b = this.ja();
	b.e(this.p, Bj, function(f) {
		var g = f.U;
		f = this.xd(g, this.Gm);
		f.Rc = g;
		this.Dc(f, h);
		f.Ma(g);
		this.hh.t(g.k(), f);
		g = this.ja();
		g.e(f, fz, this.xn);
		g.e(f, gz, this.Vm);
		if (this.p instanceof Sw) {
			g.e(f, zA, this.un);
			g.e(f, AA, this.zn)
		}
		CA(this)
	});
	b.e(this.p, Cj, function(f) {
		f = f.U;
		var g = DA(this, f);
		g && this.removeChild(g, h);
		this.hh.remove(f.k());
		CA(this)
	});
	b.e(this.p, Fj, this.Bo);
	b.e(this.p, pj, this.Oo);
	b.e(this.p, ah, this.No);
	b.e(this.p, ch, this.Ao)
}
z(BA, Io);
l = BA.prototype;
l.Jf = i;
l.yb = p;
l.Oo = function(a) {
	a = a.U;
	var b = DA(this, a.Cf);
	if (b) {
		var c = b.Gc, e = b.Qj.w(a.k(), 0), f = a.Kb;
		b.Qj.t(a.k(), f);
		if (e = c.Kb + (f - e))
			c.Kb = e;
		vz(b.q, b)
	}
	(a = DA(this, a)) && a.Si()
};
l.No = function(a) {
	var b = DA(this, a.U);
	b && Qy(b, a.message)
};
l.Ao = function(a) {
	var b = DA(this, a.U);
	b && b.Ji(a.Uo, a.Vo)
};
l.Bo = function(a) {
	var b = a.U;
	a = W(this.p, b).state;
	var c = DA(this, b);
	c && c.Ta(a);
	if (b = DA(this, b.Cf)) {
		switch (a) {
		case of:
		case pf:
		case qf:
			b.Ta(a);
			break;
		case S:
			b.ld++;
			break;
		case uf:
			b.bd != a && b.Ta(a);
			break;
		case T:
			b.ld++;
			b.xe++
		}
		if (b.ld >= b.Gc.ze)
			if (b.xe == 0) {
				b.Ta(S);
				b.p.Ga(b.Gc, S)
			} else if (b.xe == b.ld) {
				b.Ta(T);
				b.p.Ga(b.Gc, T)
			} else {
				b.Ta(tf);
				b.p.Ga(b.Gc, tf)
			}
		b.Gb()
	}
};
function DA(a, b) {
	if (!b)
		return i;
	return a.hh.w(u(b) ? b : b.k())
}
function CA(a) {
	if (a.p instanceof Px) {
		var b = yl(a);
		b && Uy(Z(a, 0)).vd(a.q.o() + wz, b == 1)
	}
}
l.xn = function(a) {
	this.p.Wb(a.target.M())
};
l.Vm = function(a) {
	jj(this.p, a.target.M())
};
l.un = function(a) {
	var b = this.p;
	a = a.target.M();
	W(b, a).Ae.pause();
	b.Ga(a, rf)
};
l.zn = function(a) {
	var b = this.p;
	a = a.target.M();
	W(b, a).Ae.cl();
	b.Ga(a, qf)
};
var EA = "Firefox/2.", FA = "..", GA = "./", HA = "/.", IA = "https", JA = "0.4.15.0", KA = "GEARS", LA = "1.9.2", MA = "XHR", NA = "JAVA", OA = "FLASH", PA = "HTML_FORM";
function QA(a, b) {
	if (Aa(a))
		d(Error("sessionServerUrl can't be empty"));
	this.Zc = a;
	this.Jb = [];
	this.Eg = undefined;
	var c;
	if (c = wc().indexOf(EA) >= 0) {
		var e = window.location;
		c = a;
		e instanceof ei || (e = ui(e));
		c instanceof ei || (c = ui(c));
		var f = e;
		c = c;
		e = f.va();
		var g = !!c.hb;
		if (g)
			fi(e, c.hb);
		else
			g = !!c.dd;
		if (g)
			gi(e, c.dd);
		else
			g = !!c.Pb;
		if (g)
			hi(e, c.Pb);
		else
			g = c.tc != i;
		var k = c.fa;
		if (g)
			ii(e, c.tc);
		else if (g = !!c.fa) {
			if (k.charAt(0) != R)
				if (f.Pb && !f.fa)
					k = R + k;
				else {
					f = e.fa.lastIndexOf(R);
					if (f != -1)
						k = e.fa.substr(0, f + 1) + k
				}
			if (k == FA || k == ba)
				k = A;
			else if (!(k.indexOf(GA) == -1 && k.indexOf(HA) == -1)) {
				f = k.lastIndexOf(R, 0) == 0;
				k = k.split(R);
				for ( var m = [], o = 0; o < k.length;) {
					var q = k[o++];
					if (q == ba)
						f && o == k.length && m.push(A);
					else if (q == FA) {
						if (m.length > 1 || m.length == 1 && m[0] != A)
							m.pop();
						f && o == k.length && m.push(A)
					} else {
						m.push(q);
						f = h
					}
				}
				k = m.join(R)
			}
		}
		if (g)
			e.Wd(k);
		else
			g = c.Ya.toString() !== A;
		if (g) {
			f = c.Ya;
			if (!f.pd)
				f.pd = f.toString() ? decodeURIComponent(f.toString()) : A;
			ji(e, f.pd, void 0)
		} else
			g = !!c.Hc;
		g && ki(e, c.Hc);
		c = e.hb == IA
	}
	!c && Vv() && Ov() && cb(Ov().version, JA) >= 0 && this.Jb.push(KA);
	if ((new Dh).upload && (!F || H(LA)))
		this.Jb.push(MA);
	b && Qj() && this.Jb.push(NA);
	!vc && Xt && cb(Yt, Pc) >= 0 && this.Jb.push(OA);
	this.Jb.push(PA)
}
function RA(a, b) {
	if (!b)
		d(Error("allowedMechanisms must be defined"));
	t(b) || (b = [ b ]);
	if (b.length == 0)
		d(Error("allowedMechanisms must not be empty"));
	C(b, function(e) {
		if (!Cb(SA, e))
			d(Error("Unknown upload mechanism: " + e))
	}, a);
	var c;
	c = fb(b, function(e) {
		return kb(this.Jb, e)
	}, a);
	if (c.length == 0)
		d(new TA("No allowed mechanism is currently available"));
	a.Jb = c
}
function UA(a, b, c) {
	a.Jm = b;
	a.Yj = c
}
function VA(a, b, c) {
	a.Lg = b;
	a.$l = c;
	a.qf = void 0
}
var WA = "-uploader", XA = "SELECT Version FROM StoreVersionInfo WHERE StoreName=?", YA = "unknown exception", ZA = "StoreVersionInfo", $A = "StoreName TEXT NOT NULL PRIMARY KEY", aB = "Version INTEGER NOT NULL";
function bB(a, b) {
	a.Eg = b;
	var c = i;
	switch (a.Jb[0]) {
	case NA:
		if (!b)
			d(new TA(
					"Because the Java uploader loads asynchronously you must specify a callback when calling createUploader."));
		new hk(w(a.nn, a), a.Lg, a.$l, a.qf);
		break;
	case MA:
		c = new ay(a.Ep);
		break;
	case KA:
		var e;
		if (a.Om)
			a: {
				c = a.Om;
				var f = c.oq() || A, g = c.mq() + WA;
				try {
					var k = new zw(f, g)
				} catch (m) {
					e = i;
					break a
				}
				k = new Fv(k, c.nq());
				try {
					e = k.v.Yo(XA, k.name) || 0
				} catch (o) {
					e = 0
				}
				if (e) {
					if (e != k.version) {
						Bv(k.v, k.v.re);
						try {
							k.Al(e);
							Zu(k, k.version);
							Cv(k.v)
						} catch (q) {
							Dv(k.v, q);
							d(Error("Could not update the " + k.name
									+ " schema  from version " + e + " to "
									+ k.version + Ew + (q.message || YA)))
						}
					}
				} else {
					Bv(k.v, k.v.re);
					try {
						Ev(k, k.Xe);
						Av(k, k.Xe);
						Av(k, [ {
							type : 1,
							name : ZA,
							kd : [ $A, aB ]
						} ], h);
						Zu(k, k.version);
						Cv(k.v)
					} catch (x) {
						Dv(k.v, x);
						d(Error("Could not create the " + k.name + " schema: "
								+ (x.message || YA)))
					}
				}
				e = k
			}
		else
			e = i;
		c = new Sw(e);
		break;
	case OA:
		if (a.Yj)
			uu = a.Yj;
		c = new qu(a.Jm);
		break;
	case PA:
		c = new Px;
		break;
	default:
		b && b(i)
	}
	if (c) {
		c.Zc = a.Zc;
		b && b(c)
	}
	return b ? i : c
}
QA.prototype.nn = function(a, b) {
	if (b) {
		a.Zc = this.Zc;
		this.Eg(a);
		this.Eg = undefined
	} else {
		this.Jb.shift();
		bB(this, this.Eg)
	}
};
var SA = {
	Ip : NA,
	Gp : KA,
	Fp : OA,
	Hp : PA,
	Kp : MA
};
function TA() {
}
z(TA, Error);
var cB = {}.Pq, dB = "apps.uploader.showDebugWindow".split(ba), eB = n;
!(dB[0] in eB) && eB.execScript && eB.execScript("var " + dB[0]);
for ( var fB; dB.length && (fB = dB.shift());)
	if (!dB.length && cB !== undefined)
		eB[fB] = cB;
	else
		eB = eB[fB] ? eB[fB] : eB[fB] = {};
var gB = /\s*;\s*/, hB = "webdriveJavaUploadEnabled", iB = ";expires=", jB = "webdriveJavaUploadEnabled=true";
function kB() {
	var a = 31536E3;
	if (/[;=\s]/.test(hB))
		d(Error('Invalid cookie name "webdriveJavaUploadEnabled"'));
	if (/[;\r\n]/.test(ac))
		d(Error('Invalid cookie value "true"'));
	a !== undefined || (a = -1);
	a = a < 0 ? A : a == 0 ? iB + (new Date(1970, 1, 1)).toUTCString() : iB
			+ (new Date(ya() + a * 1E3)).toUTCString();
	document.cookie = jB + a + A
};
function lB(a) {
	this.rp = a.token;
	this.qp = a.timepoints;
	this.Hl = a.uriPrefix;
	this.Gl = a.uriParams;
	this.Eq = a.refreshEnabled;
	this.Gq = a.refreshMinInterval;
	this.Fq = a.refreshMaxInterval;
	this.Hq = a.refreshVariance;
	this.pq = a.idleTime;
	this.kq = a.ge;
	this.lq = a.gs;
	this.Jq = a.requestTimeout;
	this.Iq = a.rrl;
	this.hg = a.locale;
	this.Sp = a.de;
	this.Pm = a.gswu;
	this.Op = (this.$p = a.email) ? a.email.toLowerCase() : A;
	this.Rq = a.statusRequestMinInterval;
	this.Qq = a.statusRequestMaxInterval;
	this.Cq = a.psd;
	this.cq = a.epc;
	this.dq = a.epm;
	this.bq = a.eit;
	this.gq = a.fsb;
	this.iq = a.gsn;
	this.tq = a.rtl;
	this.Wo = a.protocolVersion;
	this.Vp = a.dw;
	this.Tp = a.di;
	this.aq = a.fecdw;
	this.Mq = a.ri;
	this.Tq = a.nickname;
	this.qq = a.ia;
	this.Aq = a.plst;
	this.Bq = a.ptd;
	this.jd = a.cleardotUrl;
	this.Oq = a.spv;
	this.jq = a.gaiaSessionId;
	this.Np = a.assn;
	for ( var b = [], c = 0; c < b.length; c++)
		a[b[c]] = undefined
}
z(lB, Qd);
lB.prototype.Vi = i;
lB.prototype.h = function() {
	lB.a.h.call(this);
	this.Pm = this.Gl = this.qp = i
};
function us(a, b) {
	var c = [];
	if (b)
		for ( var e in b)
			c.push(Fa, e, yi, b[e]);
	return c.join(A)
};
function mB(a) {
	this.j = a;
	this.dispose = this.u
}
z(mB, Qd);
mB.prototype.Rh = p;
mB.prototype.h = function() {
	mB.a.h.call(this);
	this.j.u();
	delete this.j
};
function nB(a) {
	mB.call(this, a);
	this.sd = K(a.so);
	this.yd = i;
	this.Kd = new $s(a, this.sd);
	a = new Jp;
	a.no = 3 == i ? 3 : new $f(3, 0, 0, 0);
	a.V() && a.Ra();
	this.pi = a;
	this.wc = new Kp(i, a, Lp.O(), this.sd);
	this.wc.yg |= 64;
	this.N = new Ck(this);
	this.Yi = this.p = i
}
z(nB, mB);
l = nB.prototype;
l.Ui = h;
l.hd = j;
l.Ci = i;
l.Rh = function() {
	nB.a.Rh.call(this);
	var a = this.j, b = a.ve && oB(), c = new QA(a.gp, b);
	oB() && kB();
	c.Ep = a.he;
	UA(c, a.he, a.mp);
	b && VA(c, a.bo, a.hg);
	b = [];
	b = a.ve ? [ MA, NA, KA, OA, PA ] : [ MA, KA, OA, PA ];
	RA(c, b);
	bB(c, w(nB.prototype.Kn, this));
	this.wc.G(this.j.dp);
	if (c = a.Ap) {
		b = gb(a.yp, function(k) {
			return aq(k)
		});
		for ( var e = aq(a.vm), f = 0, g = 0; g < b.length; g++)
			if ($p(b[g], e)) {
				f = g;
				break
			}
		e = new Ut(xp.O(), this.sd);
		Vt(e, b);
		e.wg(f);
		e.G(c);
		this.Yi = e
	}
	c = this.Kd;
	c.Za();
	Ap(this.pi, c.b());
	c = this.N;
	c.e(this.wc, ol, this.qn);
	c.e(this.Kd, pe, this.pn);
	c.e(this.Kd, bt, this.Cn);
	c.e(a.he, Be, this.Jh);
	c.e(a.fe, Be, this.Dn);
	c.e(a.Bl, Be, this.en);
	c.e(a.lp, Be, this.Gn)
};
var pB = '<sup class="du-new">New!</sup>', qB = "Select folders", rB = "dragenter", sB = "dragleave", tB = "dragover", uB = "drop";
l.Kn = function(a) {
	this.p = a;
	var b = this.j;
	U(b.Dg, j);
	U(b.jl, h);
	var c = vB(this, a);
	if (this.hd = c && a.cg()) {
		b.he.innerHTML = wB(this, xB(this)) + (a.Oc() ? A : pB);
		b.fe.innerHTML = wB(this, qB) + pB;
		a.mi = Math.max(1, 3);
		aj(a);
		U(b.Ti, h);
		a.Oc() && U(b.fe, h)
	} else
		c && U(b.Bl, h);
	yB(this);
	a.Og = j;
	this.yd = new BA(a);
	this.yd.Za(this.j.wp);
	b = this.N;
	b.e(a, Fj, this.Jn);
	b.e(a, [ Bj, Cj, dj, zk ], this.Wm);
	if (zB(this, a)) {
		U(this.j.Ef, h);
		U(this.j.lg, j);
		c = this.j.bh;
		a = this.dn;
		b.e(c, rB, a);
		b.e(c, sB, a);
		b.e(c, tB, a);
		b.e(c, uB, a);
		c = document.body;
		a = this.bn;
		b.e(c, rB, a);
		b.e(c, sB, a);
		b.e(c, tB, a);
		b.e(c, uB, a)
	}
};
function vB(a, b) {
	return b instanceof hk && a.j.ve || b instanceof ay && a.j.Yg
}
function zB(a) {
	return a.j.zm && a.p.Uh() && !a.hd
}
function yB(a) {
	var b = a.p;
	if (b instanceof qu) {
		var c = b.qb;
		a = Wc(c || b.qb);
		var e = a.parentNode;
		b = Wc(b.Qk.w(c));
		jg(e, new lc(b.offsetLeft, b.offsetTop));
		e = Gg(b);
		zg(a, e.width, e.height)
	}
}
var AB = "Select files or folders", BB = "Select files to upload";
function xB(a) {
	return a.hd && !a.p.Oc() ? AB : BB
}
l.bn = function(a) {
	a.preventDefault()
};
var CB = "du-drop-target-hover", DB = "move", EB = "copy";
l.dn = function(a) {
	var b = a.Ja.dataTransfer;
	if (b && kb(b.types, Gv)) {
		switch (a.type) {
		case tB:
			J(this.j.bh, CB);
			break;
		case sB:
		case uB:
			Tc(this.j.bh, CB)
		}
		switch (a.type) {
		case tB:
		case rB:
		case sB:
			a.Ja.dataTransfer.dropEffect = F ? DB : EB;
			break;
		case uB:
			this.p.ej(a)
		}
	}
};
l.pn = function() {
	this.wc.l & 64 && this.wc.F(j)
};
l.qn = function() {
	var a = this.Kd;
	a && !a.V() && a.A(h)
};
var FB = "app", GB = '" style="vertical-align: middle">', HB = "goog-imageless-popup-button-dropdown", IB = '"/>';
l.Cn = function(a) {
	this.Ci = a.we ? a.we.id : i;
	a = a.we ? Na(a.we.name) || ns : Us;
	a = $a(sr, go, I, js, xt, sr, FB, GB, a, ur, sr, go, I, HB, IB);
	this.wc.Q().innerHTML = a
};
l.Jh = function() {
	this.p.Td(j);
	this.p.Hb()
};
l.Dn = function() {
	this.p.Oc() && this.p.Td(h);
	this.p.Hb()
};
l.en = function() {
	(new Cm(this.j, w(this.kn, this))).A(h)
};
l.kn = function() {
	kB();
	(this.sd.D.parentWindow || this.sd.D.defaultView).location.reload()
};
var JB = "du-files-selected", KB = "Upload complete.", LB = "Upload more files or folders", MB = "Upload more files", NB = "Upload more folders";
l.Wm = function(a) {
	var b = this.j.he, c = this.j.fe, e = this.j.jl, f = this.j.Ti;
	switch (a.type) {
	case Bj:
		OB(this, b, e);
		yB(this);
		break;
	case Cj:
		if (this.p.Va().length == 0) {
			b.innerHTML = wB(this, xB(this));
			c.innerHTML = wB(this, qB);
			f && U(f, h);
			Tc(e, JB);
			zB(this, this.p) || U(this.j.lg, h)
		} else
			OB(this, b, e);
		yB(this);
		break;
	case zk:
		if (!this.Ui) {
			this.N.Aa(this.p, Tu, this.Jh);
			U(this.j.Dg, j);
			U(this.j.lg, j);
			U(this.j.Dl, h);
			zB(this, this.p) && U(this.j.Ef, h);
			Dj(this.p);
			this.Ui = h
		}
		break;
	case dj:
		this.j.Dg.innerHTML = KB;
		b.innerHTML = wB(this, this.hd && !this.p.Oc() ? LB : MB);
		c.innerHTML = wB(this, NB);
		f && U(f, h);
		zB(this, this.p) && U(this.j.Ef, h);
		yB(this);
		this.p.Sd(h);
		this.Ui = j;
		this.N.e(this.p, Tu, this.Jh)
	}
};
var PB = "Select more files or folders", QB = "Select more files", RB = "Select more folders", SB = "du-uploader-warning";
function OB(a, b, c) {
	b.innerHTML = wB(a, a.hd && !a.p.Oc() ? PB : QB);
	a.j.fe.innerHTML = wB(a, RB);
	J(c, JB);
	Tc(a.j.El, SB);
	U(a.j.lg, j)
}
var TB = "\\", UB = "folderNames", VB = "rootId", WB = "cft", XB = "Creating folders...";
l.Gn = function() {
	if (vB(this, this.p)) {
		for ( var a = this.p.Va(), b = [], c = 0; c < a.length; c++) {
			var e = a[c];
			e instanceof cj ? dt(e, b) : b.push(e)
		}
		a = this.Ci;
		c = w(this.$m, this, b);
		e = {};
		for ( var f = 0; f < b.length; f++) {
			var g = b[f].Vc;
			if (g) {
				g = g.split(uc ? TB : R);
				for ( var k = 1; k <= g.length; k++)
					e[g.slice(0, k).join(R)] = h
			}
		}
		b = Bb(e);
		if (zb(b) > 0) {
			e = new li;
			e.t(UB, b.join(fe));
			a && e.t(VB, a);
			Dq(WB, e.toString(), undefined, w(et, undefined, c));
			b = h
		} else
			b = j;
		if (b) {
			YB(this, XB);
			return
		}
	}
	ZB(this, this.p.Va(), {})
};
var $B = "Uploading...";
function ZB(a, b, c) {
	var e = a.j, f = a.Yi ? a.Yi.ob().M() : i, g = i;
	if (e.Hk)
		g = e.Hk.value;
	var k = a.Ci, m = e.gm.checked;
	e = e.Gk && e.Gk.checked;
	g = g;
	for ( var o = 0; o < b.length; o++) {
		var q = b[o], x = {}, v = q.Vc;
		if (v) {
			if (v = c[v]) {
				x.parentId = v;
				q.Vc = undefined
			}
		} else if (k)
			x.parentId = k;
		x.gdConvert = String(m);
		if (e) {
			x.ocrConvert = String(e);
			if (g != i)
				x.ocrInputLanguage = g
		}
		if (f)
			x.visibilitySetting = be(f.Ac());
		q.Ck = new D(x)
	}
	if (b.length > 0) {
		YB(a, $B);
		a.p.Yk()
	} else
		J(a.j.El, SB)
}
function YB(a, b) {
	var c = a.j.Dg, e = a.j.he, f = a.j.fe, g = a.j.Ti;
	c.innerHTML = b;
	U(c, h);
	e.innerHTML = A;
	f.innerHTML = A;
	g && U(g, j);
	U(a.j.Dl, j);
	zB(a, a.p) && U(a.j.Ef, j);
	a.p.Sd(j)
}
l.$m = function(a, b) {
	ZB(this, a, b)
};
var aC = '<a href="', bC = "</a>";
l.Jn = function(a) {
	var b = W(this.p, a.U).Yc, c;
	if (b && (c = b.Rj))
		switch (W(this.p, a.U).state) {
		case S:
			if (b = c.openUrl) {
				a = cz(DA(this.yd, a.U)).b();
				a.innerHTML = aC + b + Nm + a.innerHTML + bC
			}
			break;
		case T:
			(b = c.errorMessage) && Qy(DA(this.yd, a.U), b)
		}
};
var cC = '<span class="du-upload-icon">&nbsp;</span><span class="du-select-files-text">';
function wB(a, b) {
	return cC + b + ur
}
var dC = "webdriveJavaUploadEnabled=";
function oB() {
	var a;
	a: {
		a = (document.cookie || A).split(gB);
		for ( var b = 0, c; c = a[b]; b++)
			if (c.indexOf(dC) == 0) {
				a = c.substr(26);
				break a
			}
		a = void 0
	}
	return !!a
}
l.h = function() {
	nB.a.h.call(this);
	delete this.sd;
	this.N.u();
	delete this.N;
	this.Kd.u();
	delete this.Kd;
	this.pi.u();
	delete this.pi;
	this.wc.u();
	delete this.wc;
	this.p.u();
	delete this.p;
	this.yd.u();
	delete this.yd
};
function eC(a) {
	this.jd = a.cleardotUrl;
	this.ap = a.rootDoc;
	lB.call(this, a)
}
z(eC, lB);
function fC(a) {
	this.gp = a.sessionUrl;
	this.mp = a.swfUrl;
	this.bo = a.jarUrl;
	this.Ef = a.dropMessageElement;
	this.bh = a.dropTargetElement;
	this.wp = a.uploadElement;
	this.so = a.moveToFolderElement;
	this.he = a.uploadOverlayElement;
	this.fe = a.uploadFolderOverlayElement;
	this.Bl = a.uploadFolderEnableElement;
	this.em = a.chromeDownloadUrl;
	this.Zj = a.folderUploadLearnMoreUrl;
	this.jl = a.selectFilesElement;
	this.lp = a.startUploadElement;
	this.Dl = a.uploadOptionsElement;
	this.dp = a.sfbe;
	this.gm = a.cce;
	this.Gk = a.oce;
	this.Hk = a.ocrls;
	this.El = a.ue;
	this.Dg = a.use;
	this.lg = a.nfse;
	this.yq = a.ocrft;
	this.ve = !!a.dirupa;
	this.Yg = !!a.dirupc;
	this.zm = a.drgup;
	this.Nq = a.sft;
	this.Ap = a.vse;
	this.vm = a.visibilitySetting;
	this.yp = a.validVisibilitySettings;
	this.Ti = a.uflml;
	this.nh = !!a.ficc;
	eC.call(this, a)
}
z(fC, eC);
function _createUploadApplication(a) {
	a = new fC(a);
	a = new nB(a);
	a.Rh();
	return a
}
function _createUploadApplication$clone() {
};