import { useEffect, useState } from 'react'
import api from '../services/api'

export default function Budgets() {
  const [budget, setBudget] = useState(null)
  const [loading, setLoading] = useState(true)
  const [showModal, setShowModal] = useState(false)
  const [amount, setAmount] = useState('')

  useEffect(() => {
    fetchBudget()
  }, [])

  const fetchBudget = async () => {
    try {
      const response = await api.get('/budgets')
      setBudget(response.data)
      setAmount(response.data.amount.toString())
    } catch (error) {
      if (error.response?.status === 404) {
        setBudget(null)
      } else {
        console.error('Failed to fetch budget:', error)
      }
    } finally {
      setLoading(false)
    }
  }

  const handleSubmit = async (e) => {
    e.preventDefault()
    try {
      await api.post('/budgets', { amount: parseFloat(amount) })
      setShowModal(false)
      fetchBudget()
    } catch (error) {
      console.error('Failed to save budget:', error)
      alert('Failed to save budget')
    }
  }

  const handleDelete = async () => {
    if (!window.confirm('Are you sure you want to delete your budget?')) return

    try {
      await api.delete('/budgets')
      setBudget(null)
    } catch (error) {
      console.error('Failed to delete budget:', error)
      alert('Failed to delete budget')
    }
  }

  if (loading) {
    return <div className="text-center py-12">Loading...</div>
  }

  return (
    <div className="space-y-6">
      <div className="flex justify-between items-center">
        <h1 className="text-3xl font-bold text-gray-900">Budget</h1>
        {budget ? (
          <div className="flex space-x-3">
            <button
              onClick={() => {
                setAmount(budget.amount.toString())
                setShowModal(true)
              }}
              className="inline-flex items-center px-4 py-2 border border-transparent text-sm font-medium rounded-md text-white bg-blue-600 hover:bg-blue-700"
            >
              Update Budget
            </button>
            <button
              onClick={handleDelete}
              className="inline-flex items-center px-4 py-2 border border-red-300 text-sm font-medium rounded-md text-red-700 bg-white hover:bg-red-50"
            >
              Delete Budget
            </button>
          </div>
        ) : (
          <button
            onClick={() => {
              setAmount('')
              setShowModal(true)
            }}
            className="inline-flex items-center px-4 py-2 border border-transparent text-sm font-medium rounded-md text-white bg-blue-600 hover:bg-blue-700"
          >
            Create Budget
          </button>
        )}
      </div>

      {budget ? (
        <div className="bg-white rounded-lg shadow p-6">
          <h2 className="text-2xl font-semibold mb-4">Monthly Budget</h2>
          <p className="text-4xl font-bold text-blue-600">₹{budget.amount.toFixed(2)}</p>
          {budget.lastAlertSent && (
            <p className="mt-2 text-sm text-gray-500">
              Last alert sent: {new Date(budget.lastAlertSent).toLocaleDateString()}
            </p>
          )}
        </div>
      ) : (
        <div className="bg-white rounded-lg shadow p-6 text-center">
          <p className="text-gray-500">No budget set. Create one to start tracking your spending.</p>
        </div>
      )}

      {showModal && (
        <div className="fixed inset-0 bg-gray-600 bg-opacity-50 overflow-y-auto h-full w-full z-50">
          <div className="relative top-20 mx-auto p-5 border w-96 shadow-lg rounded-md bg-white">
            <h3 className="text-lg font-bold mb-4">
              {budget ? 'Update Budget' : 'Create Budget'}
            </h3>
            <form onSubmit={handleSubmit} className="space-y-4">
              <div>
                <label className="block text-sm font-medium text-gray-700">Monthly Budget Amount</label>
                <input
                  type="number"
                  step="0.01"
                  required
                  className="mt-1 block w-full rounded-md border-gray-300 shadow-sm focus:border-blue-500 focus:ring-blue-500 sm:text-sm"
                  value={amount}
                  onChange={(e) => setAmount(e.target.value)}
                  placeholder="Enter monthly budget"
                />
              </div>
              <div className="flex space-x-3">
                <button
                  type="submit"
                  className="flex-1 bg-blue-600 text-white px-4 py-2 rounded-md hover:bg-blue-700"
                >
                  {budget ? 'Update' : 'Create'}
                </button>
                <button
                  type="button"
                  onClick={() => {
                    setShowModal(false)
                    setAmount(budget?.amount.toString() || '')
                  }}
                  className="flex-1 bg-gray-300 text-gray-700 px-4 py-2 rounded-md hover:bg-gray-400"
                >
                  Cancel
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  )
}

