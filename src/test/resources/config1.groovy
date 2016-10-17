evidence.source = './src/test/resources/check_sheet.xlsx'
evidence.sheet_name_server = 'Target'
evidence.sheet_name_rule = 'Rule'
evidence.sheet_name_spec = [
    'Linux':   'Check(Linux)',
    'Windows': 'Check(Windows)',
]
evidence.target='./build/check_sheet.xlsx'
evidence.staging_dir='./build/log'
