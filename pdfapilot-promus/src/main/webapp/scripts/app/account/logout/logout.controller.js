'use strict';

angular.module('pdfapilotpromusApp')
    .controller('LogoutController', function (Auth) {
        Auth.logout();
    });
