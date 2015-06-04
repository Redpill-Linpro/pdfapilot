'use strict';

function Splash($modal, $rootScope) {

  this.open = function (attrs, opts)  {
    var scope = $rootScope.$new();

    angular.extend(scope, attrs);

    opts = angular.extend(opts || {}, {
      backdrop: false,
      scope: scope,
      templateUrl: 'splash/content.html',
      windowTemplateUrl: 'splash/index.html'
    });

    return $modal.open(opts);
  };

}

Splash.$inject = ['$modal', '$rootScope'];

angular.module('pdfapilotpromusApp').service('splash', Splash);

angular.module('pdfapilotpromusApp').run([
  '$templateCache',
  function ($templateCache) {
    $templateCache.put('splash/index.html',
      '<section class="splash" ng-class="{\'splash-open\': animate}" ng-style="{\'z-index\': 1000, display: \'block\'}" ng-click="close($event)">' +
      ' <div class="splash-inner" ng-transclude></div>' +
      '</section>'
    );

    $templateCache.put('splash/content.html',
      '<div class="splash-content">' +
      ' <h1 ng-bind="title"></h1>' +
      ' <pre ng-bind="message"></pre>' +
      ' <button class="btn btn-lg btn-outline" ng-click="$close()">OK</button>' +
      '</div>'
    );
  }
]);
