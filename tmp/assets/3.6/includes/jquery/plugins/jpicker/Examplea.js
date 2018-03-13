
    $(function()
      {
        $.fn.jPicker.defaults.images.clientPath='images/';
        var LiveCallbackElement = $('#Live'),
            LiveCallbackButton = $('#LiveButton');
        $('#Inline').jPicker({window:{title:'Inline Example'}});
        $('#Expandable').jPicker({window:{expandable:true,title:'Expandable Example'}});
        $('#Alpha').jPicker({window:{expandable:true,title:'Alpha (Transparency) Example)',alphaSupport:true},color:{active:new $.jPicker.Color({ahex:'99330099'})}});
        $('#Binded').jPicker({window:{title:'Binded Example'},color:{active:new $.jPicker.Color({ahex:'993300ff'})}});
        $('.Multiple').jPicker({window:{title:'Multiple Binded Example'}});
        $('#Callbacks').jPicker(
          {window:{title:'Callback Example'}},
          function(color, context)
          {
            var all = color.val('all');
            alert('Color chosen - hex: ' + (all && '#' + all.hex || 'none') + ' - alpha: ' + (all && all.a + '%' || 'none'));
            $('#Commit').css({ backgroundColor: all && '#' + all.hex || 'transparent' });
          },
          function(color, context)
          {
            if (context == LiveCallbackButton.get(0)) alert('Color set from button');
            var hex = color.val('hex');
            LiveCallbackElement.css({ backgroundColor: hex && '#' + hex || 'transparent' });
          },
          function(color, context)
          {
            alert('"Cancel" Button Clicked');
          });
        $('#LiveButton').click(
          function()
          {
            $.jPicker.List[7].color.active.val('hex', 'e2ddcf', this);
          });
        $('#AlterColors').jPicker({window:{title:'Color Interaction Example'}});
        $('#GetActiveColor').click(
          function()
          {
            alert($.jPicker.List[8].color.active.val('ahex'));
          });
        $('#GetRG').click(
          function()
          {
            var rg=$.jPicker.List[8].color.active.val('rg');
            alert('red: ' + rg.r + ', green: ' + rg.g);
          });
        $('#SetHue').click(
          function()
          {
            $.jPicker.List[8].color.active.val('h', 133);
          });
        $('#SetValue').click(
          function()
          {
            $.jPicker.List[8].color.active.val('v', 38);
          });
        $('#SetRG').click(
          function()
          {
            $.jPicker.List[8].color.active.val('rg', { r: 213, g: 118 });
          });
      });
  