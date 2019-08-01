node {
  stage 'Gitクローン'
  git "${GetConfigScenarioSCM}"

  def externalMethod = load("lib/script/externalMethod.groovy")
  def branches = externalMethod.getBranches()

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
  env.TARGET_DIR = v['targetDirectory']?:v
  bat '''\
    |getconfig -x "%TARGET_DIR%\\%JOB_BASE_NAME%.zip"
    |'''.stripMargin()

}

