'use strict';

angular.module('pdfaPilotPromusApp')
    .controller('LogoutController', function (Auth) {
        Auth.logout();
    });
