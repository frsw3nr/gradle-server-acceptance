$(document).ready(function() {

    $("#platforms a.add_fields").
      data("association-insertion-position", 'before').
      data("association-insertion-node", 'this');

    $('#platforms').bind('cocoon:after-insert',
         function(e, platform) {
             console.log('inserting new platform ...');
             $(".platform-platform-fields a.add-platform").
                 data("association-insertion-position", 'after').
                 data("association-insertion-node", 'this');
             $(this).find('.platform-platform-fields').bind('cocoon:after-insert',
                  function() {
                    console.log('insert new platform ...');
                    console.log($(this));
                    $(this).find(".platform_from_list").remove();
                    $(this).find("a.add_fields").hide();
                  });
         });

});
