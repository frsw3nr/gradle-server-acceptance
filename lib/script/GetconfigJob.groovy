node {

  stage 'Gitクローン'


    // We can just run it with "externalCall(...)" since it has a call method.

  git "${GetConfigSCM}"
  bat 'git branch --set-upstream-to=origin/master master'
  bat 'git pull'


  echo "検査シナリオを設定します..."
  def externalMethod = load("lib/script/externalMethod.groovy")
  def branches = externalMethod.getBranches()
  def config_files = externalMethod.getConfigFiles()
  def v = input message: 'どの環境の検査シナリオを実行しますか?',
      ok: '検査する',
      parameters: [
        [$class: 'ChoiceParameterDefinition',
            choices: branches.join("\n"),
            description: 'Gitブランチ',
            name: 'targetBranch'],
        [$class: 'ChoiceParameterDefinition',
            choices: config_files.join("\n"),
            description: 'コンフィグファイル',
            name: '-c'],
        [$class: 'StringParameterDefinition',
            description: '検査対象の絞り込み(-s) ※オプション',
            name: '-s'],
        [$class: 'StringParameterDefinition',
            description: '検査IDの絞り込み(-t) ※オプション',
            name: '-t']
      ]
  if (v == null) {
     error '選択してください'
  }

  stage '検査シナリオ実行'

  echo "${v['targetBranch']}にスイッチします..."
  bat "git checkout ${v['targetBranch']}"

  echo "getconfig を実行します..."
  env.PATH = "c:\\server-acceptance;${env.PATH}"
  def getconfig_opt = "-c config/${v['-c']}"
  if (v['-s']) {
    getconfig_opt += ' -s ' + v['-s']
  }
  if (v['-t']) {
    getconfig_opt += ' -t ' + v['-t']
  }
  bat "getconfig ${getconfig_opt}"

  stage 'Json登録'

  echo "getconfig 実行結果を登録します..."
  bat "getconfig -u local"

  stage 'Gitコミット'

  current_env = env.getEnvironment()
  def comment = "Jenkins job=${current_env['JOB_NAME']}[${current_env['BUILD_ID']}]"
  println current_env
  bat 'git add .'
  bat "git commit -a -m \"${comment}\""
  // bat 'git push --set-upstream origin master'
  bat 'git push'
}
