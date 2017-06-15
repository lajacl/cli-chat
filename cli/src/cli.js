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
      if (cmd === 'echo') {
        this.log(cli.chalk['gray'](Message.fromJSON(buffer).toString()))
      } else if (cmd === 'broadcast') {
        this.log(cli.chalk['blue'](Message.fromJSON(buffer).toString()))
      } else if (cmd === 'users') {
        this.log(cli.chalk['cyan'](Message.fromJSON(buffer).toString()))
      } else if (cmd === 'direct') {
        this.log(cli.chalk['magenta'](Message.fromJSON(buffer).toString()))
      } else if (cmd === 'connect' || cmd === 'disconnect') {
        this.log(cli.chalk['red'](Message.fromJSON(buffer).toString()))
      } else {
        this.log(cli.chalk['white'](Message.fromJSON(buffer).toString()))
      }
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
