$ ->
  $('#inventory-table').dataTable
    processing: true
    serverSide: true
    ajax: $('#inventory-table').data('source')
    pagingType: 'full_numbers'
