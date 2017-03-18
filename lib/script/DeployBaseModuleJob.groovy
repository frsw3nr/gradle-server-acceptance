node {
  stage 'Git�N���[��'
  // git 'https://github.com/frsw3nr/gradle-server-acceptance.git'
  // git ''http://testgit001/gitbucket/git/server-acceptance/gradle-server-acceptance.git'
  git "${GetConfigBaseSCM}"

  echo "�f�v���C�V�i���I��ݒ肵�܂�..."
  def externalMethod = load("lib/script/externalMethod.groovy")
  def branches = externalMethod.getBranches()

  def v = input message: '�ǂ̊��̃f�v���C�V�i���I�����s���܂���?',
      ok: '�f�v���C����',
      parameters: [
        [$class: 'ChoiceParameterDefinition',
            choices: branches.join("\n"),
            description: 'Git�u�����`�̎w��',
            name: 'targetBranch'],
        [$class: 'BooleanParameterDefinition',
            defaultValue: false,
            description: '���j�b�g�e�X�g�̎��s�L��',
            name: 'testOption'],
        [$class: 'StringParameterDefinition',
            description: '���W���[���z�z��',
            defaultValue: 'c:\\',
            name: 'targetDirectory'],
      ]
  if (v == null) {
     error '�I�����Ă�������'
  }

  echo "${v['targetBranch']}�ɃX�C�b�`���܂�..."
  bat "git checkout ${v['targetBranch']}"

  stage '�P�̃e�X�g'
  bat 'gradle clean'
  if (v['testOption']) {
    bat 'gradle test'
  } else {
    echo '�X�L�b�v'
  }

  stage '�A�[�J�C�u'
  // set-item env:JAVA_OPTS -value '-Dgroovy.source.encoding=UTF-8 -Dfile.encoding=UTF-8'
  System.getProperties().put("JAVA_OPTS", "-Dgroovy.source.encoding=UTF-8 -Dfile.encoding=UTF-8");
  bat 'set-item env:JAVA_OPTS -value "-Dgroovy.source.encoding=UTF-8 -Dfile.encoding=UTF-8"'
  bat 'gradle shadowJar'
  bat 'gradle zipApp'

  env.TARGET_DIR = v['targetDirectory']
  bat '''\
    |cd "%TARGET_DIR%"
    |"C:\\Program Files\\7-Zip\\7z.exe" x "%WORKSPACE%\\build\\distributions\\gradle-server-acceptance-*.zip" -y
    |'''.stripMargin()

}

