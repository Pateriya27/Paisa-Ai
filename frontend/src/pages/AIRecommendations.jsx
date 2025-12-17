import { useState } from 'react'
import api from '../services/api'
import { Sparkles, Loader2 } from 'lucide-react'

export default function AIRecommendations() {
  const [recommendations, setRecommendations] = useState(null)
  const [loading, setLoading] = useState(false)

  const fetchRecommendations = async () => {
    setLoading(true)
    try {
      const response = await api.post('/ai/recommendations')
      setRecommendations(response.data)
    } catch (error) {
      console.error('Failed to fetch recommendations:', error)
      alert('Failed to get AI recommendations')
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className="space-y-6">
      <div className="flex justify-between items-center">
        <h1 className="text-3xl font-bold text-gray-900">AI Recommendations</h1>
        <button
          onClick={fetchRecommendations}
          disabled={loading}
          className="inline-flex items-center px-4 py-2 border border-transparent text-sm font-medium rounded-md text-white bg-blue-600 hover:bg-blue-700 disabled:opacity-50"
        >
          {loading ? (
            <>
              <Loader2 className="w-4 h-4 mr-2 animate-spin" />
              Loading...
            </>
          ) : (
            <>
              <Sparkles className="w-4 h-4 mr-2" />
              Get Recommendations
            </>
          )}
        </button>
      </div>

      {recommendations ? (
        <div className="space-y-6">
          {recommendations.summary && (
            <div className="bg-blue-50 border border-blue-200 rounded-lg p-6">
              <h2 className="text-lg font-semibold text-blue-900 mb-2">Summary</h2>
              <p className="text-blue-800">{recommendations.summary}</p>
            </div>
          )}

          {recommendations.recommendations && recommendations.recommendations.length > 0 && (
            <div className="bg-white rounded-lg shadow p-6">
              <h2 className="text-xl font-semibold mb-4">Recommendations</h2>
              <ul className="space-y-3">
                {recommendations.recommendations.map((rec, index) => (
                  <li key={index} className="flex items-start">
                    <span className="flex-shrink-0 w-6 h-6 rounded-full bg-blue-100 text-blue-600 flex items-center justify-center text-sm font-medium mr-3 mt-0.5">
                      {index + 1}
                    </span>
                    <p className="text-gray-700">{rec}</p>
                  </li>
                ))}
              </ul>
            </div>
          )}
        </div>
      ) : (
        <div className="bg-white rounded-lg shadow p-12 text-center">
          <Sparkles className="w-16 h-16 mx-auto text-gray-400 mb-4" />
          <p className="text-gray-500 mb-4">
            Get personalized financial recommendations based on your spending patterns
          </p>
          <button
            onClick={fetchRecommendations}
            className="inline-flex items-center px-4 py-2 border border-transparent text-sm font-medium rounded-md text-white bg-blue-600 hover:bg-blue-700"
          >
            Get Started
          </button>
        </div>
      )}
    </div>
  )
}

