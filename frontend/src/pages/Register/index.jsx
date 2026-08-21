import { useState, useRef } from 'react'
import './Style.css'
import { AiFillEyeInvisible, AiFillEye } from 'react-icons/ai'
import { FiLock } from 'react-icons/fi'
import { useRegister } from '../../hooks/useRegister'

function Home() {
  const { mutate: registerUser, isPending, isError, error: registerError } = useRegister()

  const inputName = useRef()
  const inputEmail = useRef()
  const inputPassword = useRef()
  const inputConfirmPassword = useRef()
  const inputRole = useRef()

  const [visible, setVisible] = useState(false)
  const [inputValue, setInputValue] = useState("")
  const [error, setError] = useState("")

  const handleChange = (e) => {
    const value = e.target.value
    setInputValue(value)

    if (value.length < 6) {
      setError("Password must contain at least 6 characters")
    } else {
      setError("")
    }
  }

  const toggleVisibility = () => setVisible(!visible)

  function createUser() {
    if (inputPassword.current.value !== inputConfirmPassword.current.value) {
      setError("Passwords do not match")
      return
    }

    registerUser({
      name: inputName.current.value,
      email: inputEmail.current.value,
      password: inputPassword.current.value,
      role: inputRole.current.value
    })
  }

  return (
    <div className='container'>
      <form action="">
        <h1>Cadastro de Usuários</h1>
        <input placeholder="Name" name='Name' type="text" ref={inputName} />
        <input placeholder="youremail@exemple.com" name='Email' type="email" ref={inputEmail} />

        <div className={`password-wrapper ${error ? "error" : ""}`}>
          <FiLock size={20} />

          <input
            type={visible ? "text" : "password"}
            placeholder="Password"
            ref={inputPassword}
            onChange={handleChange}
          />

          <button
            type="button"
            className="toggle-btn"
            onClick={toggleVisibility}
          >
            {visible ? <AiFillEye size={20} /> : <AiFillEyeInvisible size={20} />}
          </button>
        </div>
        {error && <small className="error-text">{error}</small>}

        <input
          placeholder="Confirm your password"
          name='ConfirmPassword'
          type="password"
          ref={inputConfirmPassword}
        />
        <input placeholder="Role" name='Role' type="text" ref={inputRole} />

        {isError && <small className="error-text">{registerError?.message ?? "Erro ao cadastrar. Tente novamente."}</small>}

        <button type='button' onClick={createUser} disabled={isPending}>
          {isPending ? "Enviando..." : "Next"}
        </button>
      </form>
    </div>
  )
}

export default Home
