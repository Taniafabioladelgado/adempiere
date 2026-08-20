(function () {
	zk.afterLoad('zul', function () {
		zul.KeyListener = zk.$extends(zk.Widget, {
			_ctkeys: '',
			_autoblur: true,

			bind_: function (desktop, skipper, after) {
				this.$supers('bind_', arguments);
				this._keydown = this.proxy(this._doKeyDown);
				jq(document).on('keydown', this._keydown);
			},

			unbind_: function () {
				if (this._keydown)
					jq(document).off('keydown', this._keydown);
				this._keydown = null;
				this.$supers('unbind_', arguments);
			},

			_doKeyDown: function (evt) {
				var keycode = evt.keyCode || evt.which,
					zkcode = this._translateKeyCode(evt, keycode);

				if (!zkcode || !this._inCtrlKeys(evt, zkcode))
					return;

				if (this._autoblur) {
					var node = this.$n();
					if (node && node.focus)
						node.focus();
				}

				this.fire('onCtrlKey', {
					keyCode: keycode,
					ctrlKey: !!evt.ctrlKey,
					shiftKey: !!evt.shiftKey,
					altKey: !!evt.altKey
				}, {toServer: true});

				this._stopEvent(evt);
				return false;
			},

			_translateKeyCode: function (evt, keycode) {
				switch (keycode) {
				case 13:
					return 'K';
				case 45:
					return 'I';
				case 46:
					return 'J';
				default:
					if (keycode >= 33 && keycode <= 40)
						return String.fromCharCode('A'.charCodeAt(0) + (keycode - 33));
					if (keycode >= 112 && keycode <= 123)
						return String.fromCharCode('P'.charCodeAt(0) + (keycode - 112));
					if (evt.ctrlKey || evt.altKey)
						return String.fromCharCode(keycode).toLowerCase();
				}
				return null;
			},

			_inCtrlKeys: function (evt, zkcode) {
				if (!this._ctkeys)
					return false;

				var prefix = evt.ctrlKey ? '^' : evt.altKey ? '@' : evt.shiftKey ? '$' : '#',
					keys = this._ctkeys,
					start = keys.indexOf(prefix),
					end = keys.indexOf(';', start + 1);

				if (start < 0 || end < 0)
					return false;
				return keys.substring(start + 1, end).indexOf(zkcode) >= 0;
			},

			_stopEvent: function (evt) {
				if (evt.stop) {
					evt.stop();
					return;
				}
				if (evt.preventDefault)
					evt.preventDefault();
				if (evt.stopPropagation)
					evt.stopPropagation();
			},

			setCtkeys: function (value) {
				this._ctkeys = value || '';
			},

			getCtkeys: function () {
				return this._ctkeys;
			},

			setAutoblur: function (value) {
				this._autoblur = value;
			},

			getAutoblur: function () {
				return this._autoblur;
			}
		});

		zul.KeyListener.molds = {
			default: function (out) {
				out.push('<div', this.domAttrs_(), '></div>');
			}
		};
	});
})();
