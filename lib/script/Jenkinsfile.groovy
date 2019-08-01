node {

	stage 'Gitクローン'

	git 'http://192.168.10.1:8090/git/root/test1.git'

  echo "検査シナリオを設定します..."
  def branches = getBranches()
  def config_files = getConfigFiles()
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
  bat "getconfig -u ${getconfig_opt}"

  stage 'Gitコミット'

  current_env = env.getEnvironment()
  def comment = "Jenkins job=${current_env['JOB_NAME']}[${current_env['BUILD_ID']}]"
  println current_env
  bat 'git add .'
  bat "git commit -a -m \"${comment}\""
  bat 'git push --set-upstream origin master'
}

def getBranches() {
  def branch_output = bat script : 'git branch -a --sort=-committerdate',
                      returnStdout : true

  def branches = []
  def branch_lines = branch_output.split("\n")
  for (ii = 0; ii < branch_lines.size() && ii < 10; ii++) {
    def line = branch_lines[ii]
    def matcher = line =~ 'remotes/origin/(.+)'
    if (matcher) {
      branches.add(matcher[0][1])
    }
  }
  if (branches.size() == 0) {
     error "ブランチが取得できません : ${branch_output}"
  }
  return branches
}

@NonCPS
def getConfigFiles() {
  def workspace = pwd()
  def config_files = []
  new File("${workspace}/config").eachFile { config_file ->
    def matcher = config_file.name =~ '(config.*\\.groovy)'
    if (matcher) {
      config_files.add(matcher[0][0])
    }
  }
  if (config_files.size() == 0) {
     error "config ファイルが取得できません : ${branch_output}"
  }
  return config_files
}
