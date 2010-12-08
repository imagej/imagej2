/*
Copyright (c) 2007, Yahoo! Inc. All rights reserved.
Code licensed under the BSD License:
http://developer.yahoo.net/yui/license.txt
version: 2.2.1
 */

if (!YAHOO.util.DragDropMgr) {
	YAHOO.util.DragDropMgr = function() {
		var Event = YAHOO.util.Event;
		return {
			ids : {},
			handleIds : {},
			dragCurrent : null,
			dragOvers : {},
			deltaX : 0,
			deltaY : 0,
			preventDefault : true,
			stopPropagation : true,
			initalized : false,
			locked : false,
			interactionInfo : null,
			init : function() {
				this.initialized = true;
			},
			POINT : 0,
			INTERSECT : 1,
			STRICT_INTERSECT : 2,
			mode : 0,
			_execOnAll : function(sMethod, args) {
				for ( var i in this.ids) {
					for ( var j in this.ids[i]) {
						var oDD = this.ids[i][j];
						if (!this.isTypeOfDD(oDD)) {
							continue;
						}
						oDD[sMethod].apply(oDD, args);
					}
				}
			},
			_onLoad : function() {
				this.init();
				Event.on(document, "mouseup", this.handleMouseUp, this, true);
				Event.on(document, "mousemove", this.handleMouseMove, this,
						true);
				Event.on(window, "unload", this._onUnload, this, true);
				Event.on(window, "resize", this._onResize, this, true);
			},
			_onResize : function(e) {
				this._execOnAll("resetConstraints", []);
			},
			lock : function() {
				this.locked = true;
			},
			unlock : function() {
				this.locked = false;
			},
			isLocked : function() {
				return this.locked;
			},
			locationCache : {},
			useCache : true,
			clickPixelThresh : 3,
			clickTimeThresh : 1000,
			dragThreshMet : false,
			clickTimeout : null,
			startX : 0,
			startY : 0,
			regDragDrop : function(oDD, sGroup) {
				if (!this.initialized) {
					this.init();
				}
				if (!this.ids[sGroup]) {
					this.ids[sGroup] = {};
				}
				this.ids[sGroup][oDD.id] = oDD;
			},
			removeDDFromGroup : function(oDD, sGroup) {
				if (!this.ids[sGroup]) {
					this.ids[sGroup] = {};
				}
				var obj = this.ids[sGroup];
				if (obj && obj[oDD.id]) {
					delete obj[oDD.id];
				}
			},
			_remove : function(oDD) {
				for ( var g in oDD.groups) {
					if (g && this.ids[g][oDD.id]) {
						delete this.ids[g][oDD.id];
					}
				}
				delete this.handleIds[oDD.id];
			},
			regHandle : function(sDDId, sHandleId) {
				if (!this.handleIds[sDDId]) {
					this.handleIds[sDDId] = {};
				}
				this.handleIds[sDDId][sHandleId] = sHandleId;
			},
			isDragDrop : function(id) {
				return (this.getDDById(id)) ? true : false;
			},
			getRelated : function(p_oDD, bTargetsOnly) {
				var oDDs = [];
				for ( var i in p_oDD.groups) {
					for (j in this.ids[i]) {
						var dd = this.ids[i][j];
						if (!this.isTypeOfDD(dd)) {
							continue;
						}
						if (!bTargetsOnly || dd.isTarget) {
							oDDs[oDDs.length] = dd;
						}
					}
				}
				return oDDs;
			},
			isLegalTarget : function(oDD, oTargetDD) {
				var targets = this.getRelated(oDD, true);
				for ( var i = 0, len = targets.length; i < len; ++i) {
					if (targets[i].id == oTargetDD.id) {
						return true;
					}
				}
				return false;
			},
			isTypeOfDD : function(oDD) {
				return (oDD && oDD.__ygDragDrop);
			},
			isHandle : function(sDDId, sHandleId) {
				return (this.handleIds[sDDId] && this.handleIds[sDDId][sHandleId]);
			},
			getDDById : function(id) {
				for ( var i in this.ids) {
					if (this.ids[i][id]) {
						return this.ids[i][id];
					}
				}
				return null;
			},
			handleMouseDown : function(e, oDD) {
				this.currentTarget = YAHOO.util.Event.getTarget(e);
				this.dragCurrent = oDD;
				var el = oDD.getEl();
				this.startX = YAHOO.util.Event.getPageX(e);
				this.startY = YAHOO.util.Event.getPageY(e);
				this.deltaX = this.startX - el.offsetLeft;
				this.deltaY = this.startY - el.offsetTop;
				this.dragThreshMet = false;
				this.clickTimeout = setTimeout(function() {
					var DDM = YAHOO.util.DDM;
					DDM.startDrag(DDM.startX, DDM.startY);
				}, this.clickTimeThresh);
			},
			startDrag : function(x, y) {
				clearTimeout(this.clickTimeout);
				if (this.dragCurrent) {
					this.dragCurrent.b4StartDrag(x, y);
					this.dragCurrent.startDrag(x, y);
				}
				this.dragThreshMet = true;
			},
			handleMouseUp : function(e) {
				if (!this.dragCurrent) {
					return;
				}
				clearTimeout(this.clickTimeout);
				if (this.dragThreshMet) {
					this.fireEvents(e, true);
				} else {
				}
				this.stopDrag(e);
				this.stopEvent(e);
			},
			stopEvent : function(e) {
				if (this.stopPropagation) {
					YAHOO.util.Event.stopPropagation(e);
				}
				if (this.preventDefault) {
					YAHOO.util.Event.preventDefault(e);
				}
			},
			stopDrag : function(e) {
				if (this.dragCurrent) {
					if (this.dragThreshMet) {
						this.dragCurrent.b4EndDrag(e);
						this.dragCurrent.endDrag(e);
					}
					this.dragCurrent.onMouseUp(e);
				}
				this.dragCurrent = null;
				this.dragOvers = {};
			},
			handleMouseMove : function(e) {
				if (!this.dragCurrent) {
					return true;
				}
				if (YAHOO.util.Event.isIE && !e.button) {
					this.stopEvent(e);
					return this.handleMouseUp(e);
				}
				if (!this.dragThreshMet) {
					var diffX = Math.abs(this.startX
							- YAHOO.util.Event.getPageX(e));
					var diffY = Math.abs(this.startY
							- YAHOO.util.Event.getPageY(e));
					if (diffX > this.clickPixelThresh
							|| diffY > this.clickPixelThresh) {
						this.startDrag(this.startX, this.startY);
					}
				}
				if (this.dragThreshMet) {
					this.dragCurrent.b4Drag(e);
					this.dragCurrent.onDrag(e);
					this.fireEvents(e, false);
				}
				this.stopEvent(e);
				return true;
			},
			fireEvents : function(e, isDrop) {
				var dc = this.dragCurrent;
				if (!dc || dc.isLocked()) {
					return;
				}
				var x = YAHOO.util.Event.getPageX(e);
				var y = YAHOO.util.Event.getPageY(e);
				var pt = new YAHOO.util.Point(x, y);
				var pos = dc.getTargetCoord(pt.x, pt.y);
				var el = dc.getDragEl();
				curRegion = new YAHOO.util.Region(pos.y,
						pos.x + el.offsetWidth, pos.y + el.offsetHeight, pos.x);
				var oldOvers = [];
				var outEvts = [];
				var overEvts = [];
				var dropEvts = [];
				var enterEvts = [];
				for ( var i in this.dragOvers) {
					var ddo = this.dragOvers[i];
					if (!this.isTypeOfDD(ddo)) {
						continue;
					}
					if (!this.isOverTarget(pt, ddo, this.mode, curRegion)) {
						outEvts.push(ddo);
					}
					oldOvers[i] = true;
					delete this.dragOvers[i];
				}
				for ( var sGroup in dc.groups) {
					if ("string" != typeof sGroup) {
						continue;
					}
					for (i in this.ids[sGroup]) {
						var oDD = this.ids[sGroup][i];
						if (!this.isTypeOfDD(oDD)) {
							continue;
						}
						if (oDD.isTarget && !oDD.isLocked() && oDD != dc) {
							if (this
									.isOverTarget(pt, oDD, this.mode, curRegion)) {
								if (isDrop) {
									dropEvts.push(oDD);
								} else {
									if (!oldOvers[oDD.id]) {
										enterEvts.push(oDD);
									} else {
										overEvts.push(oDD);
									}
									this.dragOvers[oDD.id] = oDD;
								}
							}
						}
					}
				}
				this.interactionInfo = {
					out : outEvts,
					enter : enterEvts,
					over : overEvts,
					drop : dropEvts,
					point : pt,
					draggedRegion : curRegion,
					sourceRegion : this.locationCache[dc.id],
					validDrop : isDrop
				};
				if (isDrop && !dropEvts.length) {
					this.interactionInfo.validDrop = false;
					dc.onInvalidDrop(e);
				}
				if (this.mode) {
					if (outEvts.length) {
						dc.b4DragOut(e, outEvts);
						dc.onDragOut(e, outEvts);
					}
					if (enterEvts.length) {
						dc.onDragEnter(e, enterEvts);
					}
					if (overEvts.length) {
						dc.b4DragOver(e, overEvts);
						dc.onDragOver(e, overEvts);
					}
					if (dropEvts.length) {
						dc.b4DragDrop(e, dropEvts);
						dc.onDragDrop(e, dropEvts);
					}
				} else {
					var len = 0;
					for (i = 0, len = outEvts.length; i < len; ++i) {
						dc.b4DragOut(e, outEvts[i].id);
						dc.onDragOut(e, outEvts[i].id);
					}
					for (i = 0, len = enterEvts.length; i < len; ++i) {
						dc.onDragEnter(e, enterEvts[i].id);
					}
					for (i = 0, len = overEvts.length; i < len; ++i) {
						dc.b4DragOver(e, overEvts[i].id);
						dc.onDragOver(e, overEvts[i].id);
					}
					for (i = 0, len = dropEvts.length; i < len; ++i) {
						dc.b4DragDrop(e, dropEvts[i].id);
						dc.onDragDrop(e, dropEvts[i].id);
					}
				}
			},
			getBestMatch : function(dds) {
				var winner = null;
				var len = dds.length;
				if (len == 1) {
					winner = dds[0];
				} else {
					for ( var i = 0; i < len; ++i) {
						var dd = dds[i];
						if (this.mode == this.INTERSECT && dd.cursorIsOver) {
							winner = dd;
							break;
						} else {
							if (!winner
									|| !winner.overlap
									|| (dd.overlap && winner.overlap.getArea() < dd.overlap
											.getArea())) {
								winner = dd;
							}
						}
					}
				}
				return winner;
			},
			refreshCache : function(groups) {
				var g = groups || this.ids;
				for ( var sGroup in g) {
					if ("string" != typeof sGroup) {
						continue;
					}
					for ( var i in this.ids[sGroup]) {
						var oDD = this.ids[sGroup][i];
						if (this.isTypeOfDD(oDD)) {
							var loc = this.getLocation(oDD);
							if (loc) {
								this.locationCache[oDD.id] = loc;
							} else {
								delete this.locationCache[oDD.id];
							}
						}
					}
				}
			},
			verifyEl : function(el) {
				try {
					if (el) {
						var parent = el.offsetParent;
						if (parent) {
							return true;
						}
					}
				} catch (e) {
				}
				return false;
			},
			getLocation : function(oDD) {
				if (!this.isTypeOfDD(oDD)) {
					return null;
				}
				var el = oDD.getEl(), pos, x1, x2, y1, y2, t, r, b, l;
				try {
					pos = YAHOO.util.Dom.getXY(el);
				} catch (e) {
				}
				if (!pos) {
					return null;
				}
				x1 = pos[0];
				x2 = x1 + el.offsetWidth;
				y1 = pos[1];
				y2 = y1 + el.offsetHeight;
				t = y1 - oDD.padding[0];
				r = x2 + oDD.padding[1];
				b = y2 + oDD.padding[2];
				l = x1 - oDD.padding[3];
				return new YAHOO.util.Region(t, r, b, l);
			},
			isOverTarget : function(pt, oTarget, intersect, curRegion) {
				var loc = this.locationCache[oTarget.id];
				if (!loc || !this.useCache) {
					loc = this.getLocation(oTarget);
					this.locationCache[oTarget.id] = loc;
				}
				if (!loc) {
					return false;
				}
				oTarget.cursorIsOver = loc.contains(pt);
				var dc = this.dragCurrent;
				if (!dc || (!intersect && !dc.constrainX && !dc.constrainY)) {
					return oTarget.cursorIsOver;
				}
				oTarget.overlap = null;
				if (!curRegion) {
					var pos = dc.getTargetCoord(pt.x, pt.y);
					var el = dc.getDragEl();
					curRegion = new YAHOO.util.Region(pos.y, pos.x
							+ el.offsetWidth, pos.y + el.offsetHeight, pos.x);
				}
				var overlap = curRegion.intersect(loc);
				if (overlap) {
					oTarget.overlap = overlap;
					return (intersect) ? true : oTarget.cursorIsOver;
				} else {
					return false;
				}
			},
			_onUnload : function(e, me) {
				this.unregAll();
			},
			unregAll : function() {
				if (this.dragCurrent) {
					this.stopDrag();
					this.dragCurrent = null;
				}
				this._execOnAll("unreg", []);
				for (i in this.elementCache) {
					delete this.elementCache[i];
				}
				this.elementCache = {};
				this.ids = {};
			},
			elementCache : {},
			getElWrapper : function(id) {
				var oWrapper = this.elementCache[id];
				if (!oWrapper || !oWrapper.el) {
					oWrapper = this.elementCache[id] = new this.ElementWrapper(
							YAHOO.util.Dom.get(id));
				}
				return oWrapper;
			},
			getElement : function(id) {
				return YAHOO.util.Dom.get(id);
			},
			getCss : function(id) {
				var el = YAHOO.util.Dom.get(id);
				return (el) ? el.style : null;
			},
			ElementWrapper : function(el) {
				this.el = el || null;
				this.id = this.el && el.id;
				this.css = this.el && el.style;
			},
			getPosX : function(el) {
				return YAHOO.util.Dom.getX(el);
			},
			getPosY : function(el) {
				return YAHOO.util.Dom.getY(el);
			},
			swapNode : function(n1, n2) {
				if (n1.swapNode) {
					n1.swapNode(n2);
				} else {
					var p = n2.parentNode;
					var s = n2.nextSibling;
					if (s == n1) {
						p.insertBefore(n1, n2);
					} else if (n2 == n1.nextSibling) {
						p.insertBefore(n2, n1);
					} else {
						n1.parentNode.replaceChild(n2, n1);
						p.insertBefore(n1, s);
					}
				}
			},
			getScroll : function() {
				var t, l, dde = document.documentElement, db = document.body;
				if (dde && (dde.scrollTop || dde.scrollLeft)) {
					t = dde.scrollTop;
					l = dde.scrollLeft;
				} else if (db) {
					t = db.scrollTop;
					l = db.scrollLeft;
				} else {
				}
				return {
					top : t,
					left : l
				};
			},
			getStyle : function(el, styleProp) {
				return YAHOO.util.Dom.getStyle(el, styleProp);
			},
			getScrollTop : function() {
				return this.getScroll().top;
			},
			getScrollLeft : function() {
				return this.getScroll().left;
			},
			moveToEl : function(moveEl, targetEl) {
				var aCoord = YAHOO.util.Dom.getXY(targetEl);
				YAHOO.util.Dom.setXY(moveEl, aCoord);
			},
			getClientHeight : function() {
				return YAHOO.util.Dom.getViewportHeight();
			},
			getClientWidth : function() {
				return YAHOO.util.Dom.getViewportWidth();
			},
			numericSort : function(a, b) {
				return (a - b);
			},
			_timeoutCount : 0,
			_addListeners : function() {
				var DDM = YAHOO.util.DDM;
				if (YAHOO.util.Event && document) {
					DDM._onLoad();
				} else {
					if (DDM._timeoutCount > 2000) {
					} else {
						setTimeout(DDM._addListeners, 10);
						if (document && document.body) {
							DDM._timeoutCount += 1;
						}
					}
				}
			},
			handleWasClicked : function(node, id) {
				if (this.isHandle(id, node.id)) {
					return true;
				} else {
					var p = node.parentNode;
					while (p) {
						if (this.isHandle(id, p.id)) {
							return true;
						} else {
							p = p.parentNode;
						}
					}
				}
				return false;
			}
		};
	}();
	YAHOO.util.DDM = YAHOO.util.DragDropMgr;
	YAHOO.util.DDM._addListeners();
}
(function() {
	var Event = YAHOO.util.Event;
	var Dom = YAHOO.util.Dom;
	YAHOO.util.DragDrop = function(id, sGroup, config) {
		if (id) {
			this.init(id, sGroup, config);
		}
	};
	YAHOO.util.DragDrop.prototype = {
		id : null,
		config : null,
		dragElId : null,
		handleElId : null,
		invalidHandleTypes : null,
		invalidHandleIds : null,
		invalidHandleClasses : null,
		startPageX : 0,
		startPageY : 0,
		groups : null,
		locked : false,
		lock : function() {
			this.locked = true;
		},
		unlock : function() {
			this.locked = false;
		},
		isTarget : true,
		padding : null,
		_domRef : null,
		__ygDragDrop : true,
		constrainX : false,
		constrainY : false,
		minX : 0,
		maxX : 0,
		minY : 0,
		maxY : 0,
		deltaX : 0,
		deltaY : 0,
		maintainOffset : false,
		xTicks : null,
		yTicks : null,
		primaryButtonOnly : true,
		available : false,
		hasOuterHandles : false,
		b4StartDrag : function(x, y) {
		},
		startDrag : function(x, y) {
		},
		b4Drag : function(e) {
		},
		onDrag : function(e) {
		},
		onDragEnter : function(e, id) {
		},
		b4DragOver : function(e) {
		},
		onDragOver : function(e, id) {
		},
		b4DragOut : function(e) {
		},
		onDragOut : function(e, id) {
		},
		b4DragDrop : function(e) {
		},
		onDragDrop : function(e, id) {
		},
		onInvalidDrop : function(e) {
		},
		b4EndDrag : function(e) {
		},
		endDrag : function(e) {
		},
		b4MouseDown : function(e) {
		},
		onMouseDown : function(e) {
		},
		onMouseUp : function(e) {
		},
		onAvailable : function() {
		},
		getEl : function() {
			if (!this._domRef) {
				this._domRef = Dom.get(this.id);
			}
			return this._domRef;
		},
		getDragEl : function() {
			return Dom.get(this.dragElId);
		},
		init : function(id, sGroup, config) {
			this.initTarget(id, sGroup, config);
			Event.on(this.id, "mousedown", this.handleMouseDown, this, true);
		},
		initTarget : function(id, sGroup, config) {
			this.config = config || {};
			this.DDM = YAHOO.util.DDM;
			this.groups = {};
			if (typeof id !== "string") {
				id = Dom.generateId(id);
			}
			this.id = id;
			this.addToGroup((sGroup) ? sGroup : "default");
			this.handleElId = id;
			Event.onAvailable(id, this.handleOnAvailable, this, true);
			this.setDragElId(id);
			this.invalidHandleTypes = {
				A : "A"
			};
			this.invalidHandleIds = {};
			this.invalidHandleClasses = [];
			this.applyConfig();
		},
		applyConfig : function() {
			this.padding = this.config.padding || [ 0, 0, 0, 0 ];
			this.isTarget = (this.config.isTarget !== false);
			this.maintainOffset = (this.config.maintainOffset);
			this.primaryButtonOnly = (this.config.primaryButtonOnly !== false);
		},
		handleOnAvailable : function() {
			this.available = true;
			this.resetConstraints();
			this.onAvailable();
		},
		setPadding : function(iTop, iRight, iBot, iLeft) {
			if (!iRight && 0 !== iRight) {
				this.padding = [ iTop, iTop, iTop, iTop ];
			} else if (!iBot && 0 !== iBot) {
				this.padding = [ iTop, iRight, iTop, iRight ];
			} else {
				this.padding = [ iTop, iRight, iBot, iLeft ];
			}
		},
		setInitPosition : function(diffX, diffY) {
			var el = this.getEl();
			if (!this.DDM.verifyEl(el)) {
				return;
			}
			var dx = diffX || 0;
			var dy = diffY || 0;
			var p = Dom.getXY(el);
			this.initPageX = p[0] - dx;
			this.initPageY = p[1] - dy;
			this.lastPageX = p[0];
			this.lastPageY = p[1];
			this.setStartPosition(p);
		},
		setStartPosition : function(pos) {
			var p = pos || Dom.getXY(this.getEl());
			this.deltaSetXY = null;
			this.startPageX = p[0];
			this.startPageY = p[1];
		},
		addToGroup : function(sGroup) {
			this.groups[sGroup] = true;
			this.DDM.regDragDrop(this, sGroup);
		},
		removeFromGroup : function(sGroup) {
			if (this.groups[sGroup]) {
				delete this.groups[sGroup];
			}
			this.DDM.removeDDFromGroup(this, sGroup);
		},
		setDragElId : function(id) {
			this.dragElId = id;
		},
		setHandleElId : function(id) {
			if (typeof id !== "string") {
				id = Dom.generateId(id);
			}
			this.handleElId = id;
			this.DDM.regHandle(this.id, id);
		},
		setOuterHandleElId : function(id) {
			if (typeof id !== "string") {
				id = Dom.generateId(id);
			}
			Event.on(id, "mousedown", this.handleMouseDown, this, true);
			this.setHandleElId(id);
			this.hasOuterHandles = true;
		},
		unreg : function() {
			Event.removeListener(this.id, "mousedown", this.handleMouseDown);
			this._domRef = null;
			this.DDM._remove(this);
		},
		isLocked : function() {
			return (this.DDM.isLocked() || this.locked);
		},
		handleMouseDown : function(e, oDD) {
			var button = e.which || e.button;
			if (this.primaryButtonOnly && button > 1) {
				return;
			}
			if (this.isLocked()) {
				return;
			}
			this.b4MouseDown(e);
			this.onMouseDown(e);
			this.DDM.refreshCache(this.groups);
			var pt = new YAHOO.util.Point(Event.getPageX(e), Event.getPageY(e));
			if (!this.hasOuterHandles && !this.DDM.isOverTarget(pt, this)) {
			} else {
				if (this.clickValidator(e)) {
					this.setStartPosition();
					this.DDM.handleMouseDown(e, this);
					this.DDM.stopEvent(e);
				} else {
				}
			}
		},
		clickValidator : function(e) {
			var target = Event.getTarget(e);
			return (this.isValidHandleChild(target) && (this.id == this.handleElId || this.DDM
					.handleWasClicked(target, this.id)));
		},
		getTargetCoord : function(iPageX, iPageY) {
			var x = iPageX - this.deltaX;
			var y = iPageY - this.deltaY;
			if (this.constrainX) {
				if (x < this.minX) {
					x = this.minX;
				}
				if (x > this.maxX) {
					x = this.maxX;
				}
			}
			if (this.constrainY) {
				if (y < this.minY) {
					y = this.minY;
				}
				if (y > this.maxY) {
					y = this.maxY;
				}
			}
			x = this.getTick(x, this.xTicks);
			y = this.getTick(y, this.yTicks);
			return {
				x : x,
				y : y
			};
		},
		addInvalidHandleType : function(tagName) {
			var type = tagName.toUpperCase();
			this.invalidHandleTypes[type] = type;
		},
		addInvalidHandleId : function(id) {
			if (typeof id !== "string") {
				id = Dom.generateId(id);
			}
			this.invalidHandleIds[id] = id;
		},
		addInvalidHandleClass : function(cssClass) {
			this.invalidHandleClasses.push(cssClass);
		},
		removeInvalidHandleType : function(tagName) {
			var type = tagName.toUpperCase();
			delete this.invalidHandleTypes[type];
		},
		removeInvalidHandleId : function(id) {
			if (typeof id !== "string") {
				id = Dom.generateId(id);
			}
			delete this.invalidHandleIds[id];
		},
		removeInvalidHandleClass : function(cssClass) {
			for ( var i = 0, len = this.invalidHandleClasses.length; i < len; ++i) {
				if (this.invalidHandleClasses[i] == cssClass) {
					delete this.invalidHandleClasses[i];
				}
			}
		},
		isValidHandleChild : function(node) {
			var valid = true;
			var nodeName;
			try {
				nodeName = node.nodeName.toUpperCase();
			} catch (e) {
				nodeName = node.nodeName;
			}
			valid = valid && !this.invalidHandleTypes[nodeName];
			valid = valid && !this.invalidHandleIds[node.id];
			for ( var i = 0, len = this.invalidHandleClasses.length; valid
					&& i < len; ++i) {
				valid = !Dom.hasClass(node, this.invalidHandleClasses[i]);
			}
			return valid;
		},
		setXTicks : function(iStartX, iTickSize) {
			this.xTicks = [];
			this.xTickSize = iTickSize;
			var tickMap = {};
			for ( var i = this.initPageX; i >= this.minX; i = i - iTickSize) {
				if (!tickMap[i]) {
					this.xTicks[this.xTicks.length] = i;
					tickMap[i] = true;
				}
			}
			for (i = this.initPageX; i <= this.maxX; i = i + iTickSize) {
				if (!tickMap[i]) {
					this.xTicks[this.xTicks.length] = i;
					tickMap[i] = true;
				}
			}
			this.xTicks.sort(this.DDM.numericSort);
		},
		setYTicks : function(iStartY, iTickSize) {
			this.yTicks = [];
			this.yTickSize = iTickSize;
			var tickMap = {};
			for ( var i = this.initPageY; i >= this.minY; i = i - iTickSize) {
				if (!tickMap[i]) {
					this.yTicks[this.yTicks.length] = i;
					tickMap[i] = true;
				}
			}
			for (i = this.initPageY; i <= this.maxY; i = i + iTickSize) {
				if (!tickMap[i]) {
					this.yTicks[this.yTicks.length] = i;
					tickMap[i] = true;
				}
			}
			this.yTicks.sort(this.DDM.numericSort);
		},
		setXConstraint : function(iLeft, iRight, iTickSize) {
			this.leftConstraint = parseInt(iLeft, 10);
			this.rightConstraint = parseInt(iRight, 10);
			this.minX = this.initPageX - this.leftConstraint;
			this.maxX = this.initPageX + this.rightConstraint;
			if (iTickSize) {
				this.setXTicks(this.initPageX, iTickSize);
			}
			this.constrainX = true;
		},
		clearConstraints : function() {
			this.constrainX = false;
			this.constrainY = false;
			this.clearTicks();
		},
		clearTicks : function() {
			this.xTicks = null;
			this.yTicks = null;
			this.xTickSize = 0;
			this.yTickSize = 0;
		},
		setYConstraint : function(iUp, iDown, iTickSize) {
			this.topConstraint = parseInt(iUp, 10);
			this.bottomConstraint = parseInt(iDown, 10);
			this.minY = this.initPageY - this.topConstraint;
			this.maxY = this.initPageY + this.bottomConstraint;
			if (iTickSize) {
				this.setYTicks(this.initPageY, iTickSize);
			}
			this.constrainY = true;
		},
		resetConstraints : function() {
			if (this.initPageX || this.initPageX === 0) {
				var dx = (this.maintainOffset) ? this.lastPageX
						- this.initPageX : 0;
				var dy = (this.maintainOffset) ? this.lastPageY
						- this.initPageY : 0;
				this.setInitPosition(dx, dy);
			} else {
				this.setInitPosition();
			}
			if (this.constrainX) {
				this.setXConstraint(this.leftConstraint, this.rightConstraint,
						this.xTickSize);
			}
			if (this.constrainY) {
				this.setYConstraint(this.topConstraint, this.bottomConstraint,
						this.yTickSize);
			}
		},
		getTick : function(val, tickArray) {
			if (!tickArray) {
				return val;
			} else if (tickArray[0] >= val) {
				return tickArray[0];
			} else {
				for ( var i = 0, len = tickArray.length; i < len; ++i) {
					var next = i + 1;
					if (tickArray[next] && tickArray[next] >= val) {
						var diff1 = val - tickArray[i];
						var diff2 = tickArray[next] - val;
						return (diff2 > diff1) ? tickArray[i] : tickArray[next];
					}
				}
				return tickArray[tickArray.length - 1];
			}
		},
		toString : function() {
			return ("DragDrop " + this.id);
		}
	};
})();
YAHOO.util.DD = function(id, sGroup, config) {
	if (id) {
		this.init(id, sGroup, config);
	}
};
YAHOO.extend(YAHOO.util.DD, YAHOO.util.DragDrop, {
	scroll : true,
	autoOffset : function(iPageX, iPageY) {
		var x = iPageX - this.startPageX;
		var y = iPageY - this.startPageY;
		this.setDelta(x, y);
	},
	setDelta : function(iDeltaX, iDeltaY) {
		this.deltaX = iDeltaX;
		this.deltaY = iDeltaY;
	},
	setDragElPos : function(iPageX, iPageY) {
		var el = this.getDragEl();
		this.alignElWithMouse(el, iPageX, iPageY);
	},
	alignElWithMouse : function(el, iPageX, iPageY) {
		var oCoord = this.getTargetCoord(iPageX, iPageY);
		if (!this.deltaSetXY) {
			var aCoord = [ oCoord.x, oCoord.y ];
			YAHOO.util.Dom.setXY(el, aCoord);
			var newLeft = parseInt(YAHOO.util.Dom.getStyle(el, "left"), 10);
			var newTop = parseInt(YAHOO.util.Dom.getStyle(el, "top"), 10);
			this.deltaSetXY = [ newLeft - oCoord.x, newTop - oCoord.y ];
		} else {
			YAHOO.util.Dom.setStyle(el, "left", (oCoord.x + this.deltaSetXY[0])
					+ "px");
			YAHOO.util.Dom.setStyle(el, "top", (oCoord.y + this.deltaSetXY[1])
					+ "px");
		}
		this.cachePosition(oCoord.x, oCoord.y);
		this.autoScroll(oCoord.x, oCoord.y, el.offsetHeight, el.offsetWidth);
	},
	cachePosition : function(iPageX, iPageY) {
		if (iPageX) {
			this.lastPageX = iPageX;
			this.lastPageY = iPageY;
		} else {
			var aCoord = YAHOO.util.Dom.getXY(this.getEl());
			this.lastPageX = aCoord[0];
			this.lastPageY = aCoord[1];
		}
	},
	autoScroll : function(x, y, h, w) {
		if (this.scroll) {
			var clientH = this.DDM.getClientHeight();
			var clientW = this.DDM.getClientWidth();
			var st = this.DDM.getScrollTop();
			var sl = this.DDM.getScrollLeft();
			var bot = h + y;
			var right = w + x;
			var toBot = (clientH + st - y - this.deltaY);
			var toRight = (clientW + sl - x - this.deltaX);
			var thresh = 40;
			var scrAmt = (document.all) ? 80 : 30;
			if (bot > clientH && toBot < thresh) {
				window.scrollTo(sl, st + scrAmt);
			}
			if (y < st && st > 0 && y - st < thresh) {
				window.scrollTo(sl, st - scrAmt);
			}
			if (right > clientW && toRight < thresh) {
				window.scrollTo(sl + scrAmt, st);
			}
			if (x < sl && sl > 0 && x - sl < thresh) {
				window.scrollTo(sl - scrAmt, st);
			}
		}
	},
	applyConfig : function() {
		YAHOO.util.DD.superclass.applyConfig.call(this);
		this.scroll = (this.config.scroll !== false);
	},
	b4MouseDown : function(e) {
		this.setStartPosition();
		this.autoOffset(YAHOO.util.Event.getPageX(e), YAHOO.util.Event
				.getPageY(e));
	},
	b4Drag : function(e) {
		this.setDragElPos(YAHOO.util.Event.getPageX(e), YAHOO.util.Event
				.getPageY(e));
	},
	toString : function() {
		return ("DD " + this.id);
	}
});
YAHOO.util.DDProxy = function(id, sGroup, config) {
	if (id) {
		this.init(id, sGroup, config);
		this.initFrame();
	}
};
YAHOO.util.DDProxy.dragElId = "ygddfdiv";
YAHOO.extend(YAHOO.util.DDProxy, YAHOO.util.DD, {
	resizeFrame : true,
	centerFrame : false,
	createFrame : function() {
		var self = this;
		var body = document.body;
		if (!body || !body.firstChild) {
			setTimeout(function() {
				self.createFrame();
			}, 50);
			return;
		}
		var div = this.getDragEl();
		if (!div) {
			div = document.createElement("div");
			div.id = this.dragElId;
			var s = div.style;
			s.position = "absolute";
			s.visibility = "hidden";
			s.cursor = "move";
			s.border = "2px solid #aaa";
			s.zIndex = 999;
			body.insertBefore(div, body.firstChild);
		}
	},
	initFrame : function() {
		this.createFrame();
	},
	applyConfig : function() {
		YAHOO.util.DDProxy.superclass.applyConfig.call(this);
		this.resizeFrame = (this.config.resizeFrame !== false);
		this.centerFrame = (this.config.centerFrame);
		this.setDragElId(this.config.dragElId || YAHOO.util.DDProxy.dragElId);
	},
	showFrame : function(iPageX, iPageY) {
		var el = this.getEl();
		var dragEl = this.getDragEl();
		var s = dragEl.style;
		this._resizeProxy();
		if (this.centerFrame) {
			this.setDelta(Math.round(parseInt(s.width, 10) / 2), Math
					.round(parseInt(s.height, 10) / 2));
		}
		this.setDragElPos(iPageX, iPageY);
		YAHOO.util.Dom.setStyle(dragEl, "visibility", "visible");
	},
	_resizeProxy : function() {
		if (this.resizeFrame) {
			var DOM = YAHOO.util.Dom;
			var el = this.getEl();
			var dragEl = this.getDragEl();
			var bt = parseInt(DOM.getStyle(dragEl, "borderTopWidth"), 10);
			var br = parseInt(DOM.getStyle(dragEl, "borderRightWidth"), 10);
			var bb = parseInt(DOM.getStyle(dragEl, "borderBottomWidth"), 10);
			var bl = parseInt(DOM.getStyle(dragEl, "borderLeftWidth"), 10);
			if (isNaN(bt)) {
				bt = 0;
			}
			if (isNaN(br)) {
				br = 0;
			}
			if (isNaN(bb)) {
				bb = 0;
			}
			if (isNaN(bl)) {
				bl = 0;
			}
			var newWidth = Math.max(0, el.offsetWidth - br - bl);
			var newHeight = Math.max(0, el.offsetHeight - bt - bb);
			DOM.setStyle(dragEl, "width", newWidth + "px");
			DOM.setStyle(dragEl, "height", newHeight + "px");
		}
	},
	b4MouseDown : function(e) {
		this.setStartPosition();
		var x = YAHOO.util.Event.getPageX(e);
		var y = YAHOO.util.Event.getPageY(e);
		this.autoOffset(x, y);
		this.setDragElPos(x, y);
	},
	b4StartDrag : function(x, y) {
		this.showFrame(x, y);
	},
	b4EndDrag : function(e) {
		YAHOO.util.Dom.setStyle(this.getDragEl(), "visibility", "hidden");
	},
	endDrag : function(e) {
		var DOM = YAHOO.util.Dom;
		var lel = this.getEl();
		var del = this.getDragEl();
		DOM.setStyle(del, "visibility", "");
		DOM.setStyle(lel, "visibility", "hidden");
		YAHOO.util.DDM.moveToEl(lel, del);
		DOM.setStyle(del, "visibility", "hidden");
		DOM.setStyle(lel, "visibility", "");
	},
	toString : function() {
		return ("DDProxy " + this.id);
	}
});
YAHOO.util.DDTarget = function(id, sGroup, config) {
	if (id) {
		this.initTarget(id, sGroup, config);
	}
};
YAHOO.extend(YAHOO.util.DDTarget, YAHOO.util.DragDrop, {
	toString : function() {
		return ("DDTarget " + this.id);
	}
});
YAHOO.register("dragdrop", YAHOO.util.DragDropMgr, {
	version : "2.2.1",
	build : "193"
});