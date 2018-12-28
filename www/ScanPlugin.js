var exec = require('cordova/exec');

var Scan = {
	start: function (success, error) {
		exec(success, error, 'ScanPlugin', 'start', []);
	},
	stop: function (success, error) {
		exec(success, error, 'ScanPlugin', 'stop', []);
	}
};

module.exports = Scan;

