# Lessons Learned

## PowerShell Constraints on Windows
- **Pattern:** Using `&&` in PowerShell commands (default Windows PowerShell 5.1).
- **Rule:** NEVER use `&&` or `||` to chain commands in PowerShell. These are not valid statement separators in PowerShell 5.1 and will cause a `ParserError`.
- **Solution:** Always use `;` to separate commands sequentially, or run them in separate `run_command` API calls. If conditional execution is required, use `if ($?) { command }`.
