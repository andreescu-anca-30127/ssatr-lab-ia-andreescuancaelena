
// Entities (similar to backend)
export interface User {
  id: string;
  organizer: boolean;
}

export interface Survey {
  id: string;
  userId: string; 
  feedbackStart?: string | null;
  feedbackEnd?: string | null;
}

export interface Question {
  id: string;
  text: string;
  surveyId: string;
}

export interface Form {
  id: string;
  survey: string;             
  user: string | null;         
  satisfaction: number;       
}

export interface ResponseEntity {
  id: string;
  questionId: string;
  userId: string | null;
  response: string;
}


// DTOs used by Angular
export interface SurveyWithQuestions {
  id: string;
  creator: string; 
  questions: Array<{
    id: string;
    text: string;
  }>;
}

export interface FormSubmissionPayload {
  surveyId: string;
  userId?: string;              
  satisfaction?: number | null; 
  answers: Array<{
    questionId: string;
    response: string;
  }>;
}

export interface SurveyAggregate {
  surveyId: string;
  totalResponses: number;
  averageSatisfaction: number;
  sentiment: {
    positive: number;
    neutral: number;
    negative: number;
  };
  topWords: string[];
}
