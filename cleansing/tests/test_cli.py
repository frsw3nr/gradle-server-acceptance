import pytest
from click.testing import CliRunner
from getconfig import cli


@pytest.fixture
def runner():
    return CliRunner()


def test_cli_no_opt(runner):
    result = runner.invoke(cli.main)
    print(result.output.strip())
    assert result.exit_code == -1
    # assert not result.exception
    # assert result.output.strip() == 'Hello, world.'


def test_cli_with_load_option(runner):
    ctx = []
    # result = runner.invoke(cli, ['load', 'aaa'])
    result = runner.invoke(cli, ['sync'])
    # assert not result.exception
    print ('TEST1')
    print ("RESULT : %s" % (result.output.strip()))
    assert result.exit_code == -1
    # assert result.output.strip() == 'Howdy, world.'


# def test_cli_with_arg(runner):
#     result = runner.invoke(cli.main, ['Minoru'])
#     assert result.exit_code == 0
#     assert not result.exception
#     assert result.output.strip() == 'Hello, Minoru.'
