node {
  stage 'Gitクローン'
  git "${GetConfigScenarioSCM}"
  def branches = getBranches()

  def v = input message: 'どの環境のデプロイシナリオを実行しますか?',
      ok: 'デプロイする',
      parameters: [
        [$class: 'ChoiceParameterDefinition',
            choices: branches.join("\n"),
            description: 'Gitブランチの指定',
            name: 'targetBranch'],
        [$class: 'StringParameterDefinition',
            description: 'モジュール配布先',
            defaultValue: 'c:\\getconfig',
            name: 'targetDirectory'],
      ]
  if (v == null) {
     error '選択してください'
  }

  stage 'アーカイブ'
println v
  env.TARGET_DIR = v['targetDirectory']?:v
  bat '''\
    |getconfig -x "%TARGET_DIR%\\%JOB_BASE_NAME%.zip"
    |'''.stripMargin()

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
