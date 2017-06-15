export class Message {
  static fromJSON (buffer) {
    return new Message(JSON.parse(buffer.toString()))
  }

  constructor ({ username, command, contents, timestamp }) {
    this.username = username
    this.command = command
    this.contents = contents
    this.timestamp = timestamp
  }

  toJSON () {
    return JSON.stringify({
      username: this.username,
      command: this.command,
      contents: this.contents,
      timestamp: this.timestamp
    })
  }

  txtcolor () {
    let cmd = this.command

    if (cmd === 'echo') {
      return 'black'
    } else if (cmd === 'broadcast') {
      return 'blue'
    } else if (cmd === 'users') {
      return 'gray'
    } else if (cmd === 'direct') {
      return 'magenta'
    } else if (cmd === 'connect' || Message.command === 'disconnect') {
      return 'red'
    } else return 'white'
  }

  toString () {
    if (this.command === 'echo') {
      return `${this.timestamp} <${this.username}> (echo): ${this.contents}`
    } else if (this.command === 'broadcast') {
      return `${this.timestamp} <${this.username}> (all): ${this.contents}`
    } else if (this.command === 'users') {
      return `${this.timestamp}: currently connected users:\n${this.contents}`
    } else if (this.command === 'direct') {
      return `${this.timestamp} <${this.username}> (whisper): ${this.contents}`
    } else {
      return this.contents
    }
  }
}
