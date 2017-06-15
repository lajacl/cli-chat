import vorpal from 'vorpal'
import { words } from 'lodash'
import { connect } from 'net'
import { Message } from './Message'

export const cli = vorpal()

let username
let server
let host
let port
let cmd
let cmdPrev

cli
  .delimiter(cli.chalk['yellow']('ftd~$'))

cli
  .mode('connect <username> [host] [port]')
  .delimiter(cli.chalk['green']('connected>'))
  .init(function (args, callback) {
    username = args.username
    host = '' + args.host
    port = args.port

    if ((typeof host !== 'undefined') && (typeof port !== 'undefined')) {
      server = connect({ host: host, port: port }, () => {
        server.write(new Message({ username, command: 'connect' }).toJSON() + '\n')
        callback()
      })
    } else {
      server = connect({ host: 'localhost', port: 8080 }, () => {
        server.write(new Message({ username, command: 'connect' }).toJSON() + '\n')
        callback()
      })
    }

    server.on('data', (buffer) => {
      let cmd = Message.fromJSON(buffer).command
      let txtColor = 'white'
      if (cmd === 'echo') {
        txtColor = 'cyan'
      } else if (cmd === 'broadcast') {
        txtColor = 'magenta'
      } else if (cmd === 'users') {
        txtColor = 'white'
      } else if (cmd === 'direct') {
        txtColor = 'gray'
      } else if (cmd === 'connect' || cmd === 'disconnect') {
        txtColor = 'red'
      } else {
        txtColor = 'white'
      }
      this.log(cli.chalk[txtColor](Message.fromJSON(buffer).toString()))
    })

    server.on('end', () => {
      cli.exec('exit')
    })
  })
  .action(function (input, callback) {
    const [ command, ...rest ] = words(input)
    const contents = rest.join(' ')

    if (command === 'disconnect') {
      cmd = command
      server.end(new Message({ username, command }).toJSON() + '\n')
    } else if (command === 'echo') {
      cmd = command
      server.write(new Message({ username, command, contents }).toJSON() + '\n')
    } else if (command === 'broadcast') {
      cmd = command
      server.write(new Message({ username, command, contents }).toJSON() + '\n')
    } else if (command === 'users') {
      cmd = command
      server.write(new Message({ username, command }).toJSON() + '\n')
    } else if (input.charAt(0) === '@') {
      cmd = '@' + command
      server.write(new Message({ username, command: cmd, contents }).toJSON() + '\n')
    } else if (Message.cmdPrev !== 'undefined') {
      cmd = cmdPrev
      let cnts = input
      server.write(new Message({ username, command: cmd, contents: cnts }).toJSON() + '\n')
    } else {
      cmdPrev = 'undefined'
      this.log(`A command is required.`)
    }

    cmdPrev = cmd
    callback()
  })
