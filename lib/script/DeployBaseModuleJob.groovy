node {
  stage 'Gitクローン'
  // git 'https://github.com/frsw3nr/gradle-server-acceptance.git'
  // git ''http://testgit001/gitbucket/git/server-acceptance/gradle-server-acceptance.git'
  git "${GetConfigBaseSCM}"

  echo "デプロイシナリオを設定します..."
  def externalMethod = load("lib/script/externalMethod.groovy")
  def branches = externalMethod.getBranches()

  def v = input message: 'どの環境のデプロイシナリオを実行しますか?',
      ok: 'デプロイする',
      parameters: [
        [$class: 'ChoiceParameterDefinition',
            choices: branches.join("\n"),
            description: 'Gitブランチの指定',
            name: 'targetBranch'],
        [$class: 'BooleanParameterDefinition',
            defaultValue: false,
            description: 'ユニットテストの実行有無',
            name: 'testOption'],
        [$class: 'StringParameterDefinition',
            description: 'モジュール配布先',
            defaultValue: 'c:\\',
            name: 'targetDirectory'],
      ]
  if (v == null) {
     error '選択してください'
  }

  echo "${v['targetBranch']}にスイッチします..."
  bat "git checkout ${v['targetBranch']}"

  stage '単体テスト'
  bat 'gradle clean'
  if (v['testOption']) {
    bat 'gradle test'
  } else {
    echo 'スキップ'
  }

  stage 'アーカイブ'
  bat 'gradle shadowJar'
  bat 'gradle zipApp'

  env.TARGET_DIR = v['targetDirectory']
  bat '''\
    |cd "%TARGET_DIR%"
    |"C:\\Program Files\\7-Zip\\7z.exe" x "%WORKSPACE%\\build\\distributions\\gradle-server-acceptance-*.zip" -y
    |'''.stripMargin()

}

