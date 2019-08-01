node {
  stage 'Git�N���[��'
  git "${GetConfigScenarioSCM}"

  def externalMethod = load("lib/script/externalMethod.groovy")
  def branches = externalMethod.getBranches()

  def v = input message: '�ǂ̊��̃f�v���C�V�i���I�����s���܂���?',
      ok: '�f�v���C����',
      parameters: [
        [$class: 'ChoiceParameterDefinition',
            choices: branches.join("\n"),
            description: 'Git�u�����`�̎w��',
            name: 'targetBranch'],
        [$class: 'StringParameterDefinition',
            description: '���W���[���z�z��',
            defaultValue: 'c:\\getconfig',
            name: 'targetDirectory'],
      ]
  if (v == null) {
     error '�I�����Ă�������'
  }

  stage '�A�[�J�C�u'
  env.TARGET_DIR = v['targetDirectory']?:v
  bat '''\
    |getconfig -x "%TARGET_DIR%\\%JOB_BASE_NAME%.zip"
    |'''.stripMargin()

}

